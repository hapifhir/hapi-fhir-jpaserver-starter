$('#select_flg').click(function(event){
  $("#flaglist").show();
  event.stopPropagation();
});
$(document.body).click(function (){
  $("#flaglist").hide();
});
