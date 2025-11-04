using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace CropHealth_Desktop.Models
{
    public class Report
    {
        public long id { get; set; }
        public double ndviValue { get; set; }
        public double temperatureValue { get; set; }
        public double precipitationValue { get; set; }
        public string aiInterpretation { get; set; }
        public DateTime reportDate { get; set; }

        // Associated location (read-only for coordinates)
        public Location location { get; set; }
    }
}
