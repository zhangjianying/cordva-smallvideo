var exec = require('cordova/exec');

var smallvideo ={  
	showSmallVideo:function(info,success,error){  
		exec(success, error, "SmallVideo", "showSmallVideo", [info]);  
	},
	smallVieoDeleteDir:function(info,success,error){  
		exec(success, error, "SmallVideo", "smallVieoDeleteDir", [info]);  
	},
	smallVieoPathSize:function(success){
		exec(success, function(){}, "SmallVideo", "smallVieoPathSize", []);
	}
}
module.exports = smallvideo;  