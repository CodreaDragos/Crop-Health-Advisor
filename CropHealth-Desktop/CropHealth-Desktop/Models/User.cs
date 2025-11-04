using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Serialization;

namespace CropHealth_Desktop.Models
{
    [JsonConverter(typeof(StringEnumConverter))]
    public enum Role
    {
        USER,
        ADMIN
    }

    public class User
    {
        [JsonProperty("id", DefaultValueHandling = DefaultValueHandling.Ignore)]
        public long Id { get; set; }
        
        [JsonProperty("username")]
        public string Username { get; set; }
        
        [JsonProperty("email")]
        public string email { get; set; }
        
        [JsonProperty("password")]
        public string password { get; set; }
        
        [JsonConverter(typeof(StringEnumConverter))]
        [JsonProperty("role")]
        public Role role { get; set; } = Role.USER; // Default role este USER

        [JsonProperty("locations")]
        public List<Location> locations { get; set; }

    }
}
