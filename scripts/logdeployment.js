const fs = require('fs');
const os = require("os");

const info_file = './docs/build/last_deployment_info.json';
const date = new Date().toISOString();

const content = `
{
  "version": "v1.1",
  "date": "${date}",
  "installedBy": "${os.userInfo().username}",
  "host": "${os.hostname()}"
}
`;

const info_file_dir = info_file.substring(0, info_file.lastIndexOf('/'))
if (!fs.existsSync(info_file_dir)){
  fs.mkdirSync(info_file_dir);
}

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