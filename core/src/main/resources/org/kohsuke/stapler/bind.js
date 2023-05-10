// bind tag takes care of the dependency as an adjunct

function makeStaplerProxy(url,staplerCrumb,methods) {
    if (url.substring(url.length - 1) !== '/') url+='/';
    var proxy = {};

    var stringify;
    if (Object.toJSON) // needs to use Prototype.js if it's present. See commit comment for discussion
        stringify = Object.toJSON;  // from prototype
    else if (typeof(JSON)=="object" && JSON.stringify)
        stringify = JSON.stringify; // standard

    var genMethod = function(methodName) {
        proxy[methodName] = function() {
            var args = arguments;

            // the final argument can be a callback that receives the return value
            var callback = (function(){
                if (args.length==0) return null;
                var tail = args[args.length-1];
                return (typeof(tail)=='function') ? tail : null;
            })();

            // 'arguments' is not an array so we convert it into an array
            var a = [];
            for (var i=0; i<args.length-(callback!=null?1:0); i++)
                a.push(args[i]);

            var headers = {
                'Content-Type': 'application/x-stapler-method-invocation;charset=UTF-8',
                'Crumb': staplerCrumb,
            };
            // If running in Jenkins, add Jenkins-Crumb header.
            if (typeof crumb !== 'undefined') {
                headers = crumb.wrap(headers);
            }

            fetch(url + methodName, {
                method: 'POST',
                headers: headers,
                body: stringify(a),
            })
            .then(function(response) {
                if (response.ok) {
                    var t = {
                        status: response.status,
                        statusText: response.statusText,
                    };
                    if (response.headers.has('content-type') && response.headers.get('content-type').startsWith('application/json')) {
                        response.json().then(function(responseObject) {
                            t.responseObject = function() {
                                return responseObject;
                            };
                            t.responseJSON = responseObject;
                            if (callback != null) {
                                callback(t);
                            }
                        });
                    } else {
                        response.text().then(function(responseText) {
                            t.responseText = responseText;
                            if (callback != null) {
                                callback(t);
                            }
                        });
                    }
                }
            });
        }
    };

    for(var mi = 0; mi < methods.length; mi++) {
        genMethod(methods[mi]);
    }

    return proxy;
}