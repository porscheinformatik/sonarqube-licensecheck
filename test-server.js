const http = require('http'),
  httpProxy = require('http-proxy'),
  fs = require('fs');
const proxy = httpProxy.createProxyServer({});
const server = http.createServer(function (req, res) {
  let match = req.url.match(/\/static\/licensecheck\/(\w+).js/);
  if (match) {
    res.writeHead(200, {'Content-Type': 'application/javascript'});
    fs.readFile(`./target/classes/static/${match[1]}.js`, 'utf8', function (err, data) {
      if (err) {
        return console.log(err);
      }
      res.write(data);
      res.end();
    });
  } else {
    proxy.web(req, res, {target: 'http://127.0.0.1:9000'});
  }
});
console.log("listening on port 5050");
server.listen(5050);
