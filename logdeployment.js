const fs = require('fs');
const os = require("os");

const info_file = './docs/build/last_deployment_info.json';
var date = new Date().toISOString();

var content = `
{
  "version": "v1.1",
  "date": "${date}",
  "installedBy": "${os.userInfo().username}",
  "host": "${os.hostname()}"
}
`;

fs.writeFile(info_file, content, err => {
  if (err) {
    console.error(err)
    return
  }
});

fs.readFile(info_file, 'utf8' , (err, data) => {
  if (err) {
    console.error(err)
    return
  }
  console.log(data)
})

