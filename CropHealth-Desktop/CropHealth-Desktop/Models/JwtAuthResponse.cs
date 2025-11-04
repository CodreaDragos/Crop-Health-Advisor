using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CropHealth_Desktop.Models
{
    public class JwtAuthResponse
    {
        public string accessToken { get; set; }
        public string tokenType { get; set; }
    }
}
