const express = require('express');
const cors = require('cors');
const { MongoClient } = require('mongodb');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 5000;
const MONGODB_URI = process.env.MONGODB_URI || "mongodb+srv://gulfericdonato7_db_user:KkqWIjHMKtbUSqht@eco-tap.1ardu07.mongodb.net/ecotap?retryWrites=true&w=majority";

let db, usersCol, depositsCol, redemptionsCol;

// Default Seed Users
const initialUsers = [
    {
        id: "usr_001",
        nfcCardId: "ECO-TAP-8A19B",
        pinCode: "123456",
        fullName: "Fraela Mae Aguinaldo",
        address: "Blk 12 Lot 4, Elliptical Rd, Diliman",
        barangay: "Barangay Central, Quezon City",
        ecoPoints: 380,
        totalWasteKg: 28.5,
        registeredDate: "2026-09-01"
    },
    {
        id: "usr_002",
        nfcCardId: "ECO-TAP-3F72C",
        pinCode: "654321",
        fullName: "James Ryan Balanon",
        address: "105 Mayaman St, UP Village",
        barangay: "Barangay Central, Quezon City",
        ecoPoints: 510,
        totalWasteKg: 42.0,
        registeredDate: "2026-09-01"
    },
    {
        id: "usr_003",
        nfcCardId: "ECO-TAP-9C44E",
        pinCode: "112233",
        fullName: "Rochelyn Cabatlao",
        address: "78 Kalayaan Ave, Quezon City",
        barangay: "Barangay Central, Quezon City",
        ecoPoints: 195,
        totalWasteKg: 15.2,
        registeredDate: "2026-09-02"
    },
    {
        id: "usr_004",
        nfcCardId: "ECO-TAP-7B88A",
        pinCode: "888888",
        fullName: "Remuel Fernan",
        address: "45 Matalino St, Diliman",
        barangay: "Barangay Central, Quezon City",
        ecoPoints: 640,
        totalWasteKg: 55.8,
        registeredDate: "2026-09-02"
    }
];

// Initialize MongoDB Atlas Connection
async function connectToMongoDB() {
    try {
        console.log("Connecting to MongoDB Atlas...");
        const client = new MongoClient(MONGODB_URI);
        await client.connect();
        db = client.db("ecotap");
        usersCol = db.collection("users");
        depositsCol = db.collection("deposits");
        redemptionsCol = db.collection("redemptions");

        console.log("Connected to MongoDB Atlas database: 'ecotap'");

        // Auto-seed Users if collection is empty
        const userCount = await usersCol.countDocuments();
        if (userCount === 0) {
            await usersCol.insertMany(initialUsers);
            console.log("Seeded initial users into MongoDB Atlas!");
        }
    } catch (err) {
        console.error("MongoDB Atlas Connection Error:", err.message);
    }
}

// REST API ENDPOINTS

// 1. Health Check
app.get('/api/health', (req, res) => {
    res.json({
        status: "ONLINE",
        database: db ? "CONNECTED TO MONGODB ATLAS" : "DISCONNECTED",
        service: "ECO-TAP REST API Backend",
        timestamp: new Date().toISOString()
    });
});

// 2. Get All Users
app.get('/api/users', async (req, res) => {
    try {
        const users = await usersCol.find({}).toArray();
        res.json(users);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 3. Get User by NFC Card ID
app.get('/api/users/nfc/:nfcId', async (req, res) => {
    try {
        const nfcId = req.params.nfcId.trim().toUpperCase();
        const user = await usersCol.findOne({ nfcCardId: { $regex: new RegExp(`^${nfcId}$`, 'i') } });
        if (user) res.json(user);
        else res.status(404).json({ error: "NFC User Not Found" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 4. Register New Resident
app.post('/api/users', async (req, res) => {
    try {
        const { fullName, address, nfcCardId, pinCode } = req.body;
        const newUser = {
            id: "usr_" + Math.random().toString(36).substr(2, 6),
            nfcCardId: nfcCardId.startsWith("ECO-TAP-") ? nfcCardId.toUpperCase() : `ECO-TAP-${nfcCardId.toUpperCase()}`,
            pinCode: pinCode || "123456",
            fullName,
            address,
            barangay: "Barangay Central, Quezon City",
            ecoPoints: 50,
            totalWasteKg: 0.0,
            registeredDate: new Date().toISOString().split('T')[0]
        };
        await usersCol.insertOne(newUser);
        res.status(201).json(newUser);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 5. Update User PIN
app.put('/api/users/:id/pin', async (req, res) => {
    try {
        const { newPin } = req.body;
        if (!newPin || newPin.length !== 6) {
            return res.status(400).json({ error: "PIN must be 6 digits" });
        }
        await usersCol.updateOne({ id: req.params.id }, { $set: { pinCode: newPin } });
        res.json({ message: "PIN Updated Successfully" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 6. Record Waste Deposit
app.post('/api/deposits', async (req, res) => {
    try {
        const { userId, categoryId, categoryName, weightKg, pointsEarned, nfcCardId, userName } = req.body;
        const depositTx = {
            id: "tx_dep_" + Math.random().toString(36).substr(2, 8),
            userId,
            userName,
            nfcCardId,
            categoryId,
            categoryName,
            weightKg: parseFloat(weightKg),
            pointsEarned: parseInt(pointsEarned),
            timestamp: Date.now()
        };

        await depositsCol.insertOne(depositTx);

        // Update User Eco-Points & Waste Total in MongoDB Atlas
        await usersCol.updateOne(
            { id: userId },
            {
                $inc: {
                    ecoPoints: parseInt(pointsEarned),
                    totalWasteKg: parseFloat(weightKg)
                }
            }
        );

        res.status(201).json(depositTx);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 7. Get All Deposits
app.get('/api/deposits', async (req, res) => {
    try {
        const deposits = await depositsCol.find({}).sort({ timestamp: -1 }).toArray();
        res.json(deposits);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 8. Record POS Reward Redemption
app.post('/api/redemptions', async (req, res) => {
    try {
        const { userId, itemId, itemTitle, pointsDeducted, nfcCardId, userName } = req.body;

        const user = await usersCol.findOne({ id: userId });
        if (!user || user.ecoPoints < pointsDeducted) {
            return res.status(400).json({ error: "Insufficient Eco-Points" });
        }

        const redemptionTx = {
            id: "tx_red_" + Math.random().toString(36).substr(2, 8),
            userId,
            userName,
            nfcCardId,
            itemId,
            itemTitle,
            pointsDeducted: parseInt(pointsDeducted),
            timestamp: Date.now()
        };

        await redemptionsCol.insertOne(redemptionTx);

        // Deduct points from User
        await usersCol.updateOne(
            { id: userId },
            { $inc: { ecoPoints: -parseInt(pointsDeducted) } }
        );

        res.status(201).json(redemptionTx);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 9. Get All Redemptions
app.get('/api/redemptions', async (req, res) => {
    try {
        const redemptions = await redemptionsCol.find({}).sort({ timestamp: -1 }).toArray();
        res.json(redemptions);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Start Server
app.listen(PORT, '0.0.0.0', async () => {
    await connectToMongoDB();
    console.log(`ECO-TAP MongoDB Server running on http://localhost:${PORT}`);
});
