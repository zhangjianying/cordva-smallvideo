var exec = require('cordova/exec');

var smallvideo ={  
	showSmallVideo:function(info,success,error){  
		exec(success, error, "SmallVideo", "showSmallVideo", [info]);  
	},
	smallVieoDeleteDir:function(info,success,error){  
		exec(success, error, "SmallVideo", "smallVieoDeleteDir", [info]);  
	},
	smallVieoPathSize:function(info,success,error){  
		exec(success, error, "SmallVideo", "smallVieoPathSize", [info]);  
	}
}
module.exports = smallvideo;  