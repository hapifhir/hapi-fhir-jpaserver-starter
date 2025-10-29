try {
  var currentTabIndex = sessionStorage.getItem('fhir-resource-tab-index');
} catch(exception) {
}

if (!currentTabIndex)
  currentTabIndex = '0';

$( '#tabs' ).tabs({
  active: currentTabIndex,
  activate: function( event, ui ) {
    var active = $('.selector').tabs('option', 'active');
    currentTabIndex = ui.newTab.index();
    document.activeElement.blur();
    try {
      sessionStorage.setItem('fhir-resource-tab-index', currentTabIndex);
    } catch(exception) {
    }
  }
});
