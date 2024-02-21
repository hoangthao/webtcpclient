var net = require('net')

var server = net.createServer();
server.on('connection', handleConnection);
var PORT = process.env.PORT || 9000
server.listen(PORT, function () {
    console.log('server listening to %j', server.address());
});

function handleConnection(conn) {
    var remoteAddress = conn.remoteAddress + ':' + conn.remotePort;
    console.log('new client connection from %s', remoteAddress);
    conn.setEncoding('utf8');
    conn.on('data', onConnData);
    conn.once('close', onConnClose);
    conn.on('error', onConnError);
    function onConnData(d) {
        console.log('connection data from %s: %j', remoteAddress, d);
        if (d === 'timeout5') {
            setTimeout(() => {
                conn.write(d.toUpperCase());
                conn.end()
            }, 5000);
        } else if (d === 'timeout10') {
                      setTimeout(() => {
                          conn.write(d.toUpperCase());
                          conn.end()
                      }, 10000);
                  }
        else {
            conn.write(d.toUpperCase());
            conn.end()
        }

    }
    function onConnClose() {
        console.log('connection from %s closed', remoteAddress);
    }
    function onConnError(err) {
        console.log('Connection %s error: %s', remoteAddress, err.message);
    }
}