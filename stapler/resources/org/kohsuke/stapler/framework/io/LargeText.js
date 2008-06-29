//
// Auto-scroll support for progressive log output.
//   See http://radio.javaranch.com/pascarello/2006/08/17/1155837038219.html
//
// @include org.kohsuke.stapler.framework.prototype.prototype

function AutoScroller(scrollContainer) {
    // get the height of the viewport.
    // See http://www.howtocreate.co.uk/tutorials/javascript/browserwindow
    function getViewportHeight() {
        if (typeof( window.innerWidth ) == 'number') {
            //Non-IE
            return window.innerHeight;
        } else if (document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight )) {
            //IE 6+ in 'standards compliant mode'
            return document.documentElement.clientHeight;
        } else if (document.body && ( document.body.clientWidth || document.body.clientHeight )) {
            //IE 4 compatible
            return document.body.clientHeight;
        }
        return null;
    }

    return {
        bottomThreshold : 25,
        scrollContainer: scrollContainer,

        getCurrentHeight : function() {
            var scrollDiv = $(this.scrollContainer);

            if (scrollDiv.scrollHeight > 0)
                return scrollDiv.scrollHeight;
            else
                if (objDiv.offsetHeight > 0)
                    return scrollDiv.offsetHeight;

            return null; // huh?
        },

        // return true if we are in the "stick to bottom" mode
        isSticking : function() {
            var scrollDiv = $(this.scrollContainer);
            var currentHeight = this.getCurrentHeight();

            // when used with the BODY tag, the height needs to be the viewport height, instead of
            // the element height.
            //var height = ((scrollDiv.style.pixelHeight) ? scrollDiv.style.pixelHeight : scrollDiv.offsetHeight);
            var height = getViewportHeight();
            var diff = currentHeight - scrollDiv.scrollTop - height;
            // window.alert("currentHeight=" + currentHeight + ",scrollTop=" + scrollDiv.scrollTop + ",height=" + height);

            return diff < this.bottomThreshold;
        },

        scrollToBottom : function() {
            var scrollDiv = $(this.scrollContainer);
            scrollDiv.scrollTop = this.getCurrentHeight();
        }
    };
}

// fetches the latest update from the server
function fetchNext(e,spinner,href, scroller) {
    new Ajax.Request(href, {
        method: "post",
        parameters: "start=" + e.fetchedBytes,
        onComplete: function(rsp, _) {
            // append text and do autoscroll if applicable
            var stickToBottom = scroller.isSticking();
            var text = rsp.responseText;
            if (text != "") {
                e.appendChild(document.createTextNode(text));
                if (stickToBottom) scroller.scrollToBottom();
            }

            e.fetchedBytes = rsp.getResponseHeader("X-Text-Size");
            if (rsp.getResponseHeader("X-More-Data") == "true")
                setTimeout(function() {
                    fetchNext(e,spinner,href,scroller);
                }, 1000);
            else // completed loading
            if(spinner!=null)
                spinner.style.display = "none";
        }
    });
}
