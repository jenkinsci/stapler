// bind tag takes care of the dependency as an adjunct

function makeStaplerProxy(url,crumb,methods) {
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

            if(window.jQuery === window.$) { //Is jQuery the active framework?
                $.ajax({
                    type: "POST",
                    url: url+methodName,
                    data: stringify(a),
                    contentType: 'application/x-stapler-method-invocation;charset=UTF-8',
                    headers: {'Crumb':crumb},
                    dataType: "json",
                    success: function(data, textStatus, jqXHR) {
                        if (callback!=null) {
                            var t = {};
                            t.responseObject = function() {
                                return data;
                            };
                            callback(t);
                        }
                    }
                });
            } else { //Assume prototype should work
                new Ajax.Request(url+methodName, {
                    method: 'post',
                    requestHeaders: {'Content-type':'application/x-stapler-method-invocation;charset=UTF-8','Crumb':crumb},
                    postBody: stringify(a),
                    onSuccess: function(t) {
                        if (callback!=null) {
                            t.responseObject = function() {
                                return eval('('+this.responseText+')');
                            };
                            callback(t);
                        }
                    }
                });
            }
        }
    };

    for(var mi = 0; mi < methods.length; mi++) {
        genMethod(methods[mi]);
    }

    return proxy;
}