// Services/BackendApiService.cs (Extins)
using CropHealth_Desktop.Models;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace CropHealth_Desktop.Services
{
public class BackendApiService
{
    private readonly HttpClient _httpClient;
    private const string BaseUrl = "http://localhost:8081/api/";
    private string _jwtToken;

    public BackendApiService()
    {
        _httpClient = new HttpClient { BaseAddress = new System.Uri(BaseUrl) };
        _httpClient.DefaultRequestHeaders.Accept.Clear();
        _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
    }

    public void SetAuthToken(string token)
    {
        _jwtToken = token;
        if (!string.IsNullOrEmpty(token))
        {
            _httpClient.DefaultRequestHeaders.Remove("Authorization");
            _httpClient.DefaultRequestHeaders.Authorization =
                new AuthenticationHeaderValue("Bearer", _jwtToken);
            
            System.Diagnostics.Debug.WriteLine($"✓ Auth token set: Bearer {_jwtToken.Substring(0, Math.Min(20, _jwtToken.Length))}...");
        }
        else
        {
            _httpClient.DefaultRequestHeaders.Remove("Authorization");
            System.Diagnostics.Debug.WriteLine("✗ Auth token removed");
        }
    }

    /**
     * Authenticates user and returns JWT token.
     */
    public async Task<string> LoginAsync(string email, string password)
    {
        var loginData = new LoginRequest { email = email, password = password };
        var json = JsonConvert.SerializeObject(loginData);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        var response = await _httpClient.PostAsync("auth/login", content);

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var authResponse = JsonConvert.DeserializeObject<JwtAuthResponse>(jsonResponse);

            SetAuthToken(authResponse.accessToken);
            return authResponse.accessToken;
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        throw new Exception($"Login failed. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Retrieves user information by email (requires authentication).
     */
    public async Task<User> GetUserByEmailAsync(string email)
    {
        string encodedEmail = System.Uri.EscapeDataString(email);
        var response = await _httpClient.GetAsync($"users/by-email?email={encodedEmail}");

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            
            System.Diagnostics.Debug.WriteLine($"=== JSON Response from Backend ===");
            System.Diagnostics.Debug.WriteLine(jsonResponse);
            System.Diagnostics.Debug.WriteLine($"===================================");
            
            if (jsonResponse.Contains("\"role\"") || jsonResponse.Contains("\"role\":"))
            {
                System.Diagnostics.Debug.WriteLine("✓ Field 'role' found in JSON");
            }
            else
            {
                System.Diagnostics.Debug.WriteLine("✗ Field 'role' NOT found in JSON!");
            }
            
            var settings = new JsonSerializerSettings
            {
                Converters = { new Newtonsoft.Json.Converters.StringEnumConverter() },
                MissingMemberHandling = MissingMemberHandling.Ignore,
                NullValueHandling = NullValueHandling.Ignore
            };
            
            var user = JsonConvert.DeserializeObject<User>(jsonResponse, settings);
            
            if (user != null)
            {
                System.Diagnostics.Debug.WriteLine($"User deserialized - Email: {user.email}, Role: {user.role}, Role Type: {user.role.GetType().Name}");
                
                if (user.role == Role.USER && jsonResponse.Contains("ADMIN", StringComparison.OrdinalIgnoreCase))
                {
                    System.Diagnostics.Debug.WriteLine("⚠ WARNING: JSON contains 'ADMIN' but deserialized as USER!");
                }
            }
            else
            {
                System.Diagnostics.Debug.WriteLine("✗ User object is null after deserialization!");
            }
            
            return user;
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        throw new Exception($"Failed to get user. Status: {response.StatusCode}. Details: {errorContent}");
    }

    
    /**
     * Retrieves all users.
     */
    public async Task<List<User>> GetAllUsersAsync()
    {
        System.Diagnostics.Debug.WriteLine($"GetAllUsers - Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        if (!string.IsNullOrEmpty(_jwtToken))
        {
            System.Diagnostics.Debug.WriteLine($"Authorization header: {_httpClient.DefaultRequestHeaders.Authorization}");
        }
        
        var response = await _httpClient.GetAsync("users");

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                Converters = { new Newtonsoft.Json.Converters.StringEnumConverter() },
                MissingMemberHandling = MissingMemberHandling.Ignore
            };
            var users = JsonConvert.DeserializeObject<List<User>>(jsonResponse, settings);
            return users ?? new List<User>();
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        throw new Exception($"Failed to get users. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Creates a new user (admin only, requires authentication).
     */
    public async Task<User> CreateUserAsync(User user)
    {
        System.Diagnostics.Debug.WriteLine($"Creating user - Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        if (!string.IsNullOrEmpty(_jwtToken))
        {
            System.Diagnostics.Debug.WriteLine($"Authorization header: {_httpClient.DefaultRequestHeaders.Authorization}");
        }
        
        System.Diagnostics.Debug.WriteLine($"User to create - Username: {user.Username}, Email: {user.email}, Role: {user.role}");
        
        var json = JsonConvert.SerializeObject(user, new JsonSerializerSettings
        {
            Converters = { new Newtonsoft.Json.Converters.StringEnumConverter() },
            NullValueHandling = NullValueHandling.Ignore,
            DefaultValueHandling = DefaultValueHandling.Ignore
        });
        
        System.Diagnostics.Debug.WriteLine($"JSON to send: {json}");
        
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        var response = await _httpClient.PostAsync("users", content);

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                Converters = { new Newtonsoft.Json.Converters.StringEnumConverter() },
                MissingMemberHandling = MissingMemberHandling.Ignore
            };
            return JsonConvert.DeserializeObject<User>(jsonResponse, settings);
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        System.Diagnostics.Debug.WriteLine($"✗ Create user failed - Status: {response.StatusCode}, Details: {errorContent}");
        
        string errorMessage = "Unknown error";
        if (!string.IsNullOrEmpty(errorContent))
        {
            try
            {
                if (errorContent.Trim().StartsWith("{"))
                {
                    var errorObj = JsonConvert.DeserializeObject<dynamic>(errorContent);
                    errorMessage = errorObj?.message?.ToString() ?? errorContent;
                }
                else
                {
                    errorMessage = errorContent;
                }
            }
            catch
            {
                errorMessage = errorContent;
            }
        }
        if (response.StatusCode == System.Net.HttpStatusCode.Conflict)
        {
            errorMessage = string.IsNullOrEmpty(errorMessage) || errorMessage == "Eroare necunoscută" 
                ? "User with this email or username already exists!" 
                : errorMessage;
        }
        else if (response.StatusCode == System.Net.HttpStatusCode.BadRequest)
        {
            errorMessage = string.IsNullOrEmpty(errorMessage) || errorMessage == "Unknown error"
                ? "Invalid data. Please check all fields are filled correctly!"
                : errorMessage;
        }
        
        throw new Exception($"Error: {errorMessage}");
    }

    /**
     * Updates an existing user.
     */
    public async Task<User> UpdateUserAsync(long userId, User user)
    {
        System.Diagnostics.Debug.WriteLine($"User to update - ID: {userId}, Username: {user.Username}, Email: {user.email}, Role: {user.role}");
        
        var json = JsonConvert.SerializeObject(user, new JsonSerializerSettings
        {
            Converters = { new Newtonsoft.Json.Converters.StringEnumConverter() },
            NullValueHandling = NullValueHandling.Ignore,
            DefaultValueHandling = DefaultValueHandling.Ignore
        });
        
        System.Diagnostics.Debug.WriteLine($"JSON to send: {json}");
        
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        var response = await _httpClient.PutAsync($"users/{userId}", content);

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                Converters = { new Newtonsoft.Json.Converters.StringEnumConverter() },
                MissingMemberHandling = MissingMemberHandling.Ignore
            };
            return JsonConvert.DeserializeObject<User>(jsonResponse, settings);
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        System.Diagnostics.Debug.WriteLine($"✗ Update user failed - Status: {response.StatusCode}, Details: {errorContent}");
        
        string errorMessage = "Unknown error";
        if (!string.IsNullOrEmpty(errorContent))
        {
            try
            {
                if (errorContent.Trim().StartsWith("{"))
                {
                    var errorObj = JsonConvert.DeserializeObject<dynamic>(errorContent);
                    errorMessage = errorObj?.message?.ToString() ?? errorContent;
                }
                else
                {
                    errorMessage = errorContent;
                }
            }
            catch
            {
                errorMessage = errorContent;
            }
        }
        if (response.StatusCode == System.Net.HttpStatusCode.Conflict)
        {
            errorMessage = string.IsNullOrEmpty(errorMessage) || errorMessage == "Unknown error" 
                ? "User with this email or username already exists!" 
                : errorMessage;
        }
        else if (response.StatusCode == System.Net.HttpStatusCode.BadRequest)
        {
            errorMessage = string.IsNullOrEmpty(errorMessage) || errorMessage == "Eroare necunoscută"
                ? "Date invalide. Verificați că toate câmpurile sunt completate corect!"
                : errorMessage;
        }
        
        throw new Exception($"Error: {errorMessage}");
    }

    /**
     * Deletes a user by ID.
     */
    public async Task<bool> DeleteUserAsync(long userId)
    {
        System.Diagnostics.Debug.WriteLine($"DeleteUser - ID: {userId}, Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        var response = await _httpClient.DeleteAsync($"users/{userId}");

        if (response.IsSuccessStatusCode)
        {
            return true;
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        throw new Exception($"Failed to delete user. Status: {response.StatusCode}. Details: {errorContent}");
    }

    
    /**
     * Retrieves all locations.
     */
    public async Task<List<Location>> GetAllLocationsAsync()
    {
        System.Diagnostics.Debug.WriteLine($"GetAllLocations - Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            throw new Exception("You are not authenticated. Please login again.");
        }
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        var response = await _httpClient.GetAsync("locations");

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                MissingMemberHandling = MissingMemberHandling.Ignore,
                NullValueHandling = NullValueHandling.Ignore
            };
            var locations = JsonConvert.DeserializeObject<List<Location>>(jsonResponse, settings);
            return locations ?? new List<Location>();
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception("Session expired or you are not authenticated. Please login again.");
        }
        throw new Exception($"Failed to get locations. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Creates a new location.
     */
    public async Task<Location> CreateLocationAsync(Location location)
    {
        System.Diagnostics.Debug.WriteLine($"Creating location - Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            System.Diagnostics.Debug.WriteLine("✗ CreateLocationAsync: Token is null or empty!");
            throw new Exception("You are not authenticated. Please login again.");
        }
        
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        var authHeader = _httpClient.DefaultRequestHeaders.Authorization;
        if (authHeader != null)
        {
            System.Diagnostics.Debug.WriteLine($"✓ Authorization header set: {authHeader.Scheme} {authHeader.Parameter?.Substring(0, Math.Min(20, authHeader.Parameter?.Length ?? 0))}...");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine("✗ Authorization header is NULL after setting!");
        }
        
        var json = JsonConvert.SerializeObject(location, new JsonSerializerSettings
        {
            NullValueHandling = NullValueHandling.Ignore,
            DefaultValueHandling = DefaultValueHandling.Ignore
        });
        
        System.Diagnostics.Debug.WriteLine($"Sending POST to locations with JSON: {json}");
        
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        // Create request manually and set Authorization header directly
        var request = new HttpRequestMessage(HttpMethod.Post, new Uri(_httpClient.BaseAddress, "locations"))
        {
            Content = content
        };
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        if (request.Headers.Authorization != null)
        {
            System.Diagnostics.Debug.WriteLine($"✓ Request Authorization header: {request.Headers.Authorization.Scheme} {request.Headers.Authorization.Parameter?.Substring(0, Math.Min(20, request.Headers.Authorization.Parameter?.Length ?? 0))}...");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine("✗ Request Authorization header is NULL!");
        }
        
        var response = await _httpClient.SendAsync(request);
        
        System.Diagnostics.Debug.WriteLine($"Response status: {response.StatusCode}");
        
        string errorBody = null;
        if (!response.IsSuccessStatusCode)
        {
            errorBody = await response.Content.ReadAsStringAsync();
            System.Diagnostics.Debug.WriteLine($"Error response body: {errorBody}");
        }

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                MissingMemberHandling = MissingMemberHandling.Ignore
            };
            return JsonConvert.DeserializeObject<Location>(jsonResponse, settings);
        }

        var errorContent = errorBody ?? await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception($"Session expired or you are not authenticated. Please login again. Details: {errorContent}");
        }
        throw new Exception($"Failed to create location. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Updates an existing location.
     */
    public async Task<Location> UpdateLocationAsync(Location location)
    {
        System.Diagnostics.Debug.WriteLine($"Updating location - ID: {location.id}, Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            System.Diagnostics.Debug.WriteLine("✗ UpdateLocationAsync: Token is null or empty!");
            throw new Exception("You are not authenticated. Please login again.");
        }
        
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        var authHeader = _httpClient.DefaultRequestHeaders.Authorization;
        if (authHeader != null)
        {
            System.Diagnostics.Debug.WriteLine($"✓ Authorization header set: {authHeader.Scheme} {authHeader.Parameter?.Substring(0, Math.Min(20, authHeader.Parameter?.Length ?? 0))}...");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine("✗ Authorization header is NULL after setting!");
        }
        
        var json = JsonConvert.SerializeObject(location, new JsonSerializerSettings
        {
            NullValueHandling = NullValueHandling.Ignore,
            DefaultValueHandling = DefaultValueHandling.Ignore
        });
        
        System.Diagnostics.Debug.WriteLine($"Sending PUT to locations with JSON: {json}");
        
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        // Create request manually and set Authorization header directly
        var request = new HttpRequestMessage(HttpMethod.Put, new Uri(_httpClient.BaseAddress, "locations"))
        {
            Content = content
        };
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        if (request.Headers.Authorization != null)
        {
            System.Diagnostics.Debug.WriteLine($"✓ Request Authorization header: {request.Headers.Authorization.Scheme} {request.Headers.Authorization.Parameter?.Substring(0, Math.Min(20, request.Headers.Authorization.Parameter?.Length ?? 0))}...");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine("✗ Request Authorization header is NULL!");
        }
        
        var response = await _httpClient.SendAsync(request);
        
        System.Diagnostics.Debug.WriteLine($"Response status: {response.StatusCode}");
        
        string errorBody = null;
        if (!response.IsSuccessStatusCode)
        {
            errorBody = await response.Content.ReadAsStringAsync();
            System.Diagnostics.Debug.WriteLine($"Error response body: {errorBody}");
        }

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                MissingMemberHandling = MissingMemberHandling.Ignore
            };
            return JsonConvert.DeserializeObject<Location>(jsonResponse, settings);
        }

        var errorContent = errorBody ?? await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception($"Session expired or you are not authenticated. Please login again. Details: {errorContent}");
        }
        throw new Exception($"Failed to update location. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Deletes a location by ID.
     */
    public async Task<bool> DeleteLocationAsync(long locationId)
    {
        System.Diagnostics.Debug.WriteLine($"DeleteLocation - ID: {locationId}, Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            throw new Exception("You are not authenticated. Please login again.");
        }
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        var response = await _httpClient.DeleteAsync($"locations/{locationId}");

        if (response.IsSuccessStatusCode)
        {
            return true;
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception("Session expired or you are not authenticated. Please login again.");
        }
        throw new Exception($"Failed to delete location. Status: {response.StatusCode}. Details: {errorContent}");
    }

    
    /**
     * Retrieves all reports.
     */
    public async Task<List<Report>> GetAllReportsAsync()
    {
        System.Diagnostics.Debug.WriteLine($"GetAllReports - Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            throw new Exception("You are not authenticated. Please login again.");
        }
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        var response = await _httpClient.GetAsync("reports/all");

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                MissingMemberHandling = MissingMemberHandling.Ignore,
                NullValueHandling = NullValueHandling.Ignore,
                DateFormatString = "yyyy-MM-ddTHH:mm:ss"
            };
            var reports = JsonConvert.DeserializeObject<List<Report>>(jsonResponse, settings);
            return reports ?? new List<Report>();
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception("Session expired or you are not authenticated. Please login again.");
        }
        throw new Exception($"Failed to get reports. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Creates a new report.
     */
    public async Task<Report> CreateReportAsync(Report report)
    {
        System.Diagnostics.Debug.WriteLine($"Creating report - Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            System.Diagnostics.Debug.WriteLine("✗ CreateReportAsync: Token is null or empty!");
            throw new Exception("You are not authenticated. Please login again.");
        }
        
        var json = JsonConvert.SerializeObject(report, new JsonSerializerSettings
        {
            NullValueHandling = NullValueHandling.Ignore,
            DefaultValueHandling = DefaultValueHandling.Ignore,
            DateFormatString = "yyyy-MM-ddTHH:mm:ss"
        });
        
        System.Diagnostics.Debug.WriteLine($"Sending POST to reports with JSON: {json}");
        
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        // Create request manually and set Authorization header directly
        var request = new HttpRequestMessage(HttpMethod.Post, new Uri(_httpClient.BaseAddress, "reports"))
        {
            Content = content
        };
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        if (request.Headers.Authorization != null)
        {
            System.Diagnostics.Debug.WriteLine($"✓ Request Authorization header: {request.Headers.Authorization.Scheme} {request.Headers.Authorization.Parameter?.Substring(0, Math.Min(20, request.Headers.Authorization.Parameter?.Length ?? 0))}...");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine("✗ Request Authorization header is NULL!");
        }
        
        var response = await _httpClient.SendAsync(request);
        
        System.Diagnostics.Debug.WriteLine($"Response status: {response.StatusCode}");
        
        string errorBody = null;
        if (!response.IsSuccessStatusCode)
        {
            errorBody = await response.Content.ReadAsStringAsync();
            System.Diagnostics.Debug.WriteLine($"Error response body: {errorBody}");
        }

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                MissingMemberHandling = MissingMemberHandling.Ignore,
                DateFormatString = "yyyy-MM-ddTHH:mm:ss"
            };
            return JsonConvert.DeserializeObject<Report>(jsonResponse, settings);
        }

        var errorContent = errorBody ?? await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception($"Session expired or you are not authenticated. Please login again. Details: {errorContent}");
        }
        throw new Exception($"Failed to create report. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Updates an existing report.
     */
    public async Task<Report> UpdateReportAsync(Report report)
    {
        System.Diagnostics.Debug.WriteLine($"Updating report - ID: {report.id}, Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            System.Diagnostics.Debug.WriteLine("✗ UpdateReportAsync: Token is null or empty!");
            throw new Exception("You are not authenticated. Please login again.");
        }
        
        var json = JsonConvert.SerializeObject(report, new JsonSerializerSettings
        {
            NullValueHandling = NullValueHandling.Ignore,
            DefaultValueHandling = DefaultValueHandling.Ignore,
            DateFormatString = "yyyy-MM-ddTHH:mm:ss"
        });
        
        System.Diagnostics.Debug.WriteLine($"Sending PUT to reports with JSON: {json}");
        
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        // Create request manually and set Authorization header directly
        var request = new HttpRequestMessage(HttpMethod.Put, new Uri(_httpClient.BaseAddress, "reports"))
        {
            Content = content
        };
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        if (request.Headers.Authorization != null)
        {
            System.Diagnostics.Debug.WriteLine($"✓ Request Authorization header: {request.Headers.Authorization.Scheme} {request.Headers.Authorization.Parameter?.Substring(0, Math.Min(20, request.Headers.Authorization.Parameter?.Length ?? 0))}...");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine("✗ Request Authorization header is NULL!");
        }
        
        var response = await _httpClient.SendAsync(request);
        
        System.Diagnostics.Debug.WriteLine($"Response status: {response.StatusCode}");
        
        string errorBody = null;
        if (!response.IsSuccessStatusCode)
        {
            errorBody = await response.Content.ReadAsStringAsync();
            System.Diagnostics.Debug.WriteLine($"Error response body: {errorBody}");
        }

        if (response.IsSuccessStatusCode)
        {
            var jsonResponse = await response.Content.ReadAsStringAsync();
            var settings = new JsonSerializerSettings
            {
                MissingMemberHandling = MissingMemberHandling.Ignore,
                DateFormatString = "yyyy-MM-ddTHH:mm:ss"
            };
            return JsonConvert.DeserializeObject<Report>(jsonResponse, settings);
        }

        var errorContent = errorBody ?? await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception($"Session expired or you are not authenticated. Please login again. Details: {errorContent}");
        }
        throw new Exception($"Failed to update report. Status: {response.StatusCode}. Details: {errorContent}");
    }

    /**
     * Deletes a report by ID.
     */
    public async Task<bool> DeleteReportAsync(long reportId)
    {
        System.Diagnostics.Debug.WriteLine($"DeleteReport - ID: {reportId}, Token present: {!string.IsNullOrEmpty(_jwtToken)}");
        
        if (string.IsNullOrEmpty(_jwtToken))
        {
            throw new Exception("You are not authenticated. Please login again.");
        }
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        _httpClient.DefaultRequestHeaders.Authorization = 
            new AuthenticationHeaderValue("Bearer", _jwtToken);
        
        var response = await _httpClient.DeleteAsync($"reports/{reportId}");

        if (response.IsSuccessStatusCode)
        {
            return true;
        }

        var errorContent = await response.Content.ReadAsStringAsync();
        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
        {
            throw new Exception("Session expired or you are not authenticated. Please login again.");
        }
        throw new Exception($"Failed to delete report. Status: {response.StatusCode}. Details: {errorContent}");
    }
}
}