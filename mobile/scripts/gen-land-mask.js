const fs = require('fs');
const path = require('path');
const sharp = require('sharp');
const { geoEquirectangular, geoPath } = require('d3-geo');

const W = 4096, H = 2048;
const land = JSON.parse(fs.readFileSync(path.join(__dirname, 'land.json'), 'utf8'));
const proj = geoEquirectangular().scale(W / (2 * Math.PI)).translate([W / 2, H / 2]).precision(0.1);
const d = geoPath(proj)(land);

const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}"><rect width="${W}" height="${H}" fill="#000"/><path d="${d}" fill="#fff"/><rect y="${H * 175 / 180}" width="${W}" height="${H / 36}" fill="#fff"/></svg>`;

sharp(Buffer.from(svg), { limitInputPixels: false })
  .blur(2)
  .resize(W / 2, H / 2)
  .greyscale()
  .png({ compressionLevel: 9 })
  .toFile(path.join(__dirname, '..', 'assets', 'land-mask.png'))
  .then(i => console.log('land-mask.png', i.width + 'x' + i.height, i.size + ' bytes'));
