const express = require('express');
const path = require('path');
const functions = require('firebase-functions');

const app = express();

app.use(express.static(path.join(__dirname, '..', 'docs')));

exports.app = functions.https.onRequest(app);
