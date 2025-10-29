if (document.documentElement.dir=='rtl') {
  var r = document.querySelector(':root');

  r.style.setProperty('--ig-left', 'right');    
  r.style.setProperty('--ig-right', 'left');
}
