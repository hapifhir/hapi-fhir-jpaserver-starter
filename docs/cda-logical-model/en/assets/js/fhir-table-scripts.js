let fhirTableLoading = false;

function getCollapsed(store, row) {
  return sessionStorage.getItem("ft-"+store+row);
}

function setCollapsed(store, row, value) {
  if (!fhirTableLoading) {
    if (value == 'collapsed') {
      sessionStorage.setItem("ft-"+store+row, value);
    } else {
      sessionStorage.removeItem("ft-"+store+row);
    }
  }
}
  
function fhirTableRowExpand(table, id) {
  var rows = table.getElementsByTagName('tr');
  var row, i;
  var noex = null;
  for (i = 0; i < rows.length; i++) {
    row = rows[i];
    if (row.id.startsWith(id)) {
      if (noex && row.id.startsWith(noex)) {
        // do nothing
      } else {
        noex = null;
        if (row.id != id) {
          row.style.display = "";
          if (row.className == 'closed') {
            noex = row.id;
          }
        }
      }
    }
  }
}

function fhirTableRowCollapse(table, id) {
  var rows = table.getElementsByTagName('tr');
  var row, i;
  for (i = 0; i < rows.length; i++) {
    row = rows[i];
    if (row.id.startsWith(id) && row.id != id) {
      row.style.display = "none";
    }
  }
}

function findElementFromFocus(src, name) {
  e = src;
  while (e && e.tagName != name) {
    e = e.parentNode;
  }
  return e;
}

// src - a handle to an element in a row in the table 
function tableRowAction(src) {
  let table = findElementFromFocus(src, "TABLE");
  let row = findElementFromFocus(src, "TR");
  let td = row.firstElementChild;
  let state = row.className;
  if (state == "closed") {
    fhirTableRowExpand(table, row.id);
    row.className = "open";
    src.src  = src.src.replace("-closed", "-open");
    td.style.backgroundImage = td.style.backgroundImage.replace('0.png', '1.png');
    setCollapsed(table.id, row.id, 'expanded');
  } else {
    fhirTableRowCollapse(table, row.id);
    row.className = "closed";
    src.src  = src.src.replace("-open", "-closed");
    td.style.backgroundImage = td.style.backgroundImage.replace('1.png', '0.png');
    setCollapsed(table.id, row.id, 'collapsed');
  }
}

// src - a handle to an element in a row in the table 
function fhirTableInit(src) {
  let table = findElementFromFocus(src, "TABLE");
  var rows = table.getElementsByTagName('tr');
  var row, i;
  fhirTableLoading = true;
  for (i = 0; i < rows.length; i++) {
    row = rows[i];
    var id = row.id;
    if (getCollapsed(table.id, id) == 'collapsed') {
      let td = row.firstElementChild;
      let e = td.firstElementChild;
      while (e.tagName != "IMG" || !(e.src.includes("join"))) {
        e = e.nextSibling;
      }
      tableRowAction(e);
    }
  }
  fhirTableLoading = false;
}

function filterTree(table, text) {
  if (!text) {
     for (let i = 1; i < table.rows.length-1; i++) {
      const row = table.rows[i];
      row.style.display = '';
      const cell = row.cells[4];
      cell.style.display = '';
    }
  } else if (text.startsWith('.')) {
    text = text.substring(1);
    for (let i = 1; i < table.rows.length-1; i++) {
      const row = table.rows[i];
      let rowText = row.textContent || row.innerText
      rowText = rowText.toLowerCase();

      // Check if row contains the search text
      if (rowText.includes(text)) {
        let id = row.id;
        while (id) {
          document.getElementById(id).style.display = '';
          id = id.substring(0, id.length - 1);
        }
      } else {
        // Hide the row
        row.style.display = 'none';
      }
    }
  } else {
    for (let i = 1; i < table.rows.length-1; i++) {
      const row = table.rows[i];
      const cell = row.cells[4];
      let cellText = cell.textContent || cell.innerText
      cellText = cellText.toLowerCase();

      // Check if row contains the search text
      if (cellText.includes(text)) {
        // Show the row
        cell.style.display = '';
      } else {
        // Hide the row
        cell.style.display = 'none';
      }
    }
  }
}

function filterDesc(table, prop, value, panel) {
  let v = 'none';
  if (value) {
    v = '';
  }
  
  panel.style.display = 'none';
  localStorage.setItem('ht-table-states-'+prop, value);

  for (let i = 1; i < table.rows.length-1; i++) {
    const row = table.rows[i];
    const cell = row.cells[4];
    if (cell) {
      for (let i = 0; i < cell.children.length; i++) {
        const childElement = cell.children[i];
        let role = childElement.getAttribute('data-role');
        if (role == prop) {
          childElement.style.display = v;
        }
      }
    }
  }
}

function hide() {
  if (visiblePanel) {
    visiblePanel.style.display = 'none';
    visiblePanel = null;
  }
}

function showPanel(button, table, panel) {
  const rect1 = button.getBoundingClientRect();
  panel.style.top = (rect1.bottom+10) + 'px';
  panel.style.left = (rect1.left) + 'px';
  panel.style.display = 'block';
  visiblePanel = panel;
  window.addEventListener('scroll', hide);
  window.addEventListener('click', hide);
  event.stopPropagation();
}
