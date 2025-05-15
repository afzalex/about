const fs = require('fs');
const path = require('path');

// Read the template
const templatePath = path.join(__dirname, 'templates', 'success.html');
let template = fs.readFileSync(templatePath, 'utf8');

// Replace variables
template = template
    .replace(/{{REPOSITORY}}/g, process.env.REPOSITORY)
    .replace(/{{RUN_ID}}/g, process.env.RUN_ID)
    .replace(/{{FILES_LIST}}/g, process.env.FILES_LIST);

// Write the processed template to stdout
console.log(template); 