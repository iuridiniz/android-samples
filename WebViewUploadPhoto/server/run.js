#!/usr/bin/env node

var formidable = require('formidable');
var http = require('http');
var util = require('util');
var fs = require('fs');

http.createServer(function(req, res) {
    var url;
    var method;
    if (req.url == "/")
        url = "/index.html";
    else
        url = req.url;

    method = req.method.toLowerCase();

    if (url == '/upload_file' && method == 'post') {
        // parse a file upload
        var form = new formidable.IncomingForm();
        form.parse(req, function(err, fields, files) {
            res.writeHead(200, {'content-type': 'text/plain'});
            res.end('received upload:\n\n');

            console.log('received upload:\n\n');
            console.log(util.inspect({fields: fields, files: files}));
        });
        return;
    }

    // else: serve a file from public
    var filepath = __dirname + "/../public" + url;
    fs.readFile(filepath, function(err, data) {
        if (err && err.code == "ENOENT") {
            res.writeHead(404, {'content-type': 'text/plain'});
            res.end("url " + url + " does not exist here");
            return
        }
        if (err) {
            console.log("ERR: unable to process ", url, "\n", err);
            res.writeHead(404, {'content-type': 'text/plain'});
            res.end("Cannot process yout request");
            return
        }
        res.writeHead(200);
        res.end(data);
        return
    });
    
}).listen(9002, '0.0.0.0');

console.log('Server running at http://127.0.0.1:9002/');



