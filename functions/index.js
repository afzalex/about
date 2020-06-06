const express = require('express');
const path = require('path');
const functions = require('firebase-functions');
const nodemailer = require("nodemailer");

const app = express();

app.use(express.static(path.join(__dirname, '..', 'docs')));

app.get("/test", function (req, res) {
    res.status(200).json({
        data: req.query.data,
        path: req.params.name,
        time: new Date().toString(),
        ip: req.ip,
        ips: req.ips
    });
});



(function () {

    app.get('/sender', function (req, res) {
        let testAccount = {
            user: 'nicholas49@ethereal.email',
            pass: 'JGDPRtWhKu7EeY8Rfp'
        };
        // nodemailer.createTestAccount()
        //     .then(testAccount => {
        //         res.status(200).json({
        //             version: 'v2',
        //             time: new Date().toString(),
        //             username: testAccount.user,
        //             password: testAccount.pass
        //         });
        //         main(testAccount, req.query.data);
        //     });
        
        res.status(200).json({
            version: 'v3',
            time: new Date().toString(),
            username: testAccount.user,
            password: testAccount.pass
        });
        main(testAccount, req.query.data);
    });


    // async..await is not allowed in global scope, must use a wrapper
    async function main(testAccount, data) {
        // Generate test SMTP service account from ethereal.email
        // Only needed if you don't have a real mail account for testing
        //let testAccount = await nodemailer.createTestAccount();

        // create reusable transporter object using the default SMTP transport
        let transporter = nodemailer.createTransport({
            host: "smtp.ethereal.email",
            port: 587,
            secure: false, // true for 465, false for other ports
            auth: {
                user: testAccount.user, // generated ethereal user
                pass: testAccount.pass // generated ethereal password
            }
        });

        if (!data) {
            data = "Hello World !";
        }

        // send mail with defined transport object
        let info = transporter.sendMail({
            from: '"Fred Foo 👻" <foo@example.com>', // sender address
            to: "bar@example.com, baz@example.com", // list of receivers
            subject: "Hello ✔", // Subject line
            text: data, // plain text body
            html: "<b>" + data + "</b>" // html body
        });

        console.log("Message sent: %s", info.messageId);
        // Message sent: <b658f8ca-6296-ccf4-8306-87d57a0b4321@example.com>

        // Preview only available when sending through an Ethereal account
        console.log("Preview URL: %s", nodemailer.getTestMessageUrl(info));
        // Preview URL: https://ethereal.email/message/WaQKMgKddxQDoou...
    }

    //main().catch(console.error);
})();



exports.app = functions.https.onRequest(app);


//transporter.sendMail({from: '"Fred Foo " <foo@example.com>',to: "bar@example.com, baz@example.com",subject: "Hello ✔",text: "Hello World!",html: "<b>Hello World!</b>"});

