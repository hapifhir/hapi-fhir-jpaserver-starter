document.addEventListener('DOMContentLoaded', function() {
  const mermaidCodes = document.querySelectorAll('pre.language-mermaid code.language-mermaid');
  Array.from(mermaidCodes).forEach(function(code) {
    const pre = code.parentNode;
    const content = code.textContent;
    const mermaidDiv = document.createElement('div');
    mermaidDiv.className = 'mermaid';
    mermaidDiv.textContent = content;
    pre.parentNode.replaceChild(mermaidDiv, pre);
  });
  mermaid.initialize({ securityLevel: 'sandbox' });
});
