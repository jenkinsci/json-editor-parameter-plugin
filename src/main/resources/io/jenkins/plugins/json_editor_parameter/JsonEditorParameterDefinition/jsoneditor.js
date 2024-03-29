Behaviour.specify(".editor-holder", "json-editor-parameter", 0, function(element) {

    const holderElement = element;
    const name = holderElement.dataset.name;
    const valueElement = document.getElementById(`editor:${name}:value`)

    const options = JSON.parse(document.getElementById(`editor:${name}:options`).textContent)
    valueElement.value = JSON.stringify(options.startval)

    const editor = new JSONEditor(holderElement, options)
    editor.on('change', function() { valueElement.value = JSON.stringify(editor.getValue()) } );
});