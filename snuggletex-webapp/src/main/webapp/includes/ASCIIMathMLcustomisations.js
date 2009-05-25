/* (This is a shameless rip-off of the version Keith did for COSMaP!) */

function AMdisplayQuoted(inputNodeId,outputNodeId,now) {
  if (document.getElementById(inputNodeId) != null) {
    if (AMkeyspressed == 5 || now) {
      var str = "` "+document.getElementById(inputNodeId).value+" `";
      var outnode = document.getElementById(outputNodeId);
      var newnode = AMcreateElementXHTML("div");
      newnode.setAttribute("id",outputNodeId);
      outnode.parentNode.replaceChild(newnode,outnode);
      outnode = document.getElementById(outputNodeId);
      var n = outnode.childNodes.length;
      for (var i = 0; i < n; i++)
        outnode.removeChild(outnode.firstChild);
      outnode.appendChild(document.createComment(str+"``"));
      LMprocessNode(outnode,true);
      AMprocessNode(outnode,true);
      AMkeyspressed = 0;
    } else AMkeyspressed++;
  }
}

function submitMathML(outputNodeId, formElementId) {
  var outputNode = document.getElementById(outputNodeId);
  var mathNode = outputNode.getElementsByTagName("math")[0];  // get the first math element inside th+    cleanedMathML = AMnode2string(mathNode,"").replace(/\r/g,"");  // replace all /r with entity
  var cleanedMathML = AMnode2string(mathNode,"");

  var hiddenFormElement = document.getElementById(formElementId);
  hiddenFormElement.value = cleanedMathML;
  return true;
}
