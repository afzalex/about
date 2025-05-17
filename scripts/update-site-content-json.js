const fs = require('fs');
const mammoth = require('mammoth');
const cheerio = require('cheerio');

async function extractSectionsFromDocx(filePath) {
  const buffer = fs.readFileSync(filePath);
  const result = await mammoth.convertToHtml({ buffer });

  const $ = cheerio.load(result.value);
  const sections = {};
  let currentHeader = null;

  $('body').children().each((_, el) => {
    const tag = $(el).get(0).tagName;

    if (/^h[1-6]$/.test(tag)) {
      currentHeader = $(el).text().trim();
      sections[currentHeader] = [];
    } else if (currentHeader) {
      sections[currentHeader].push(el);
    }
  });

  return [sections, $];
}

extractSectionsFromDocx('docs/assets/resume-sde.docx')
  .then(([sections, $]) => {
    // ✅ Example: Get specific header content
    const content = {
        'title': $(sections['Mohammad Afzal']).html().split('<br>')[0],
        'summary': sections['Summary'].map(it => `<p>${$(it).html().trim()}</p>`).join()
    }

    fs.writeFileSync('docs/assets/content.json', JSON.stringify(content, null, 2));
    console.log('Content updated successfully');
    console.log(content);
  })
  .catch(err => {
    console.error("Error:", err);
    process.exit(1);
  });
