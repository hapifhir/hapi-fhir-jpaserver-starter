doRedirect();

function doRedirect() {
  var userLang = navigator.language || navigator.userLanguage;
  var path = window.location.pathname;
  var pageName = path.substring(path.lastIndexOf('/') + 1);
  for (i=0;i<langs.length;i++) {
    if ((userLang == langs[i]) || userLang.startsWith(langs[i]+"-")) {
      window.location.replace(langs[i]+"/"+pageName);
      return;
    }
  }
  window.location.replace(langs[0]+"/"+pageName);
}
