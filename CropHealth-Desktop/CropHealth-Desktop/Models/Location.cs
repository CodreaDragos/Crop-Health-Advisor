using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using CropHealth_Desktop.Models;

namespace CropHealth_Desktop.Models
{
    public class Location
    {
        public long id { get; set; }
        public double latitude { get; set; }
        public double longitude { get; set; }
        public string name { get; set; }

        // Note: Relationship with User is required
        public User user { get; set; }

        // List of reports (can be ignored or included as needed)
        public List<Report> reports { get; set; }
    }
}
