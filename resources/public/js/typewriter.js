// http://stackoverflow.com/questions/11656980/simulate-scrolling-text-in-javascript/11657799#11657799
function displayText(id, text) {
    var node = document.createTextNode(""),
    i = 0,
    chars = 1;
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    tr.appendChild(td);
    td.appendChild(node);
    document.getElementById(id).appendChild(tr);

    (function add(){
	var toAdd = text.substr(i, chars);
	if (toAdd == "\n") {
	    document.getElementById(id).appendChild(document.createElement("br"));
	} else {
            node.data += toAdd;
	}
        i += chars;
        if (i < text.length) {
            setTimeout(add, 3000/60);
	}
    })();
}
