 
      // Travel Dashboard Module - COMPLETE VERSION
      window.TravelDashboard = (function () {
        let originalData = null;
        let currentData = null;
        let sortStates = {
          culture: "default",
          food: "default",
          attractions: "default",
        };

        // Utility functions
        const sanitizeText = (text) => {
          if (
            !text ||
            text === "N/A" ||
            text === "undefined" ||
            text === "null"
          )
            return "";
          const div = document.createElement("div");
          div.textContent = String(text);
          return div.innerHTML;
        };

        const parseNumericValue = (value) => {
          if (typeof value === "number") return value;
          if (typeof value === "string") {
            const parsed = parseFloat(value.replace(/[^\d.-]/g, ""));
            return isNaN(parsed) ? 0 : parsed;
          }
          return 0;
        };

        const formatDate = (date) => {
          return date.toISOString().split("T")[0];
        };

        // Initialize default dates - FIXED
        const initializeDates = () => {
          const today = new Date();
          const tomorrow = new Date(today);
          tomorrow.setDate(today.getDate() + 1);

          const weekLater = new Date(tomorrow);
          weekLater.setDate(tomorrow.getDate() + 7);

          // Set proper YYYY-MM-DD format
          document.getElementById("from").value = formatDate(tomorrow);
          document.getElementById("to").value = formatDate(weekLater);

          // Set min date to today to prevent past dates
          const todayStr = formatDate(today);
          document.getElementById("from").min = todayStr;
          document.getElementById("to").min = todayStr;
        };

        // Validation functions - COMPREHENSIVE
        const showFieldError = (fieldName, message) => {
          const field = document.getElementById(fieldName);
          const errorDiv = document.getElementById(`${fieldName}Error`);

          if (field && errorDiv) {
            field.classList.add(
              "border-red-500",
              "focus:ring-red-500",
              "focus:border-red-500"
            );
            field.classList.remove(
              "border-gray-200",
              "focus:ring-blue-500",
              "focus:border-transparent"
            );
            errorDiv.textContent = message;
            errorDiv.classList.remove("hidden");
            field.setAttribute("aria-invalid", "true");
          }
        };

        const hideFieldError = (fieldName) => {
          const field = document.getElementById(fieldName);
          const errorDiv = document.getElementById(`${fieldName}Error`);

          if (field && errorDiv) {
            field.classList.remove(
              "border-red-500",
              "focus:ring-red-500",
              "focus:border-red-500"
            );
            field.classList.add(
              "border-gray-200",
              "focus:ring-blue-500",
              "focus:border-transparent"
            );
            errorDiv.classList.add("hidden");
            field.removeAttribute("aria-invalid");
          }
        };

        const validateForm = (formData) => {
          let isValid = true;

          // Clear all previous errors
          ["country", "city", "from", "to"].forEach((field) =>
            hideFieldError(field)
          );

          // Required fields validation
          if (!formData.country?.trim()) {
            showFieldError("country", "Country is required");
            isValid = false;
          }

          if (!formData.city?.trim()) {
            showFieldError("city", "City is required");
            isValid = false;
          }

          // Date validation - FIXED LOGIC
          const today = new Date();
          today.setHours(0, 0, 0, 0);

          let fromDate, toDate;

          if (formData.from) {
            fromDate = new Date(formData.from);
            if (isNaN(fromDate.getTime())) {
              showFieldError("from", "Please select a valid from date");
              isValid = false;
            } else if (fromDate < today) {
              showFieldError("from", "From date cannot be in the past");
              isValid = false;
            }
          }

          if (formData.to) {
            toDate = new Date(formData.to);
            if (isNaN(toDate.getTime())) {
              showFieldError("to", "Please select a valid to date");
              isValid = false;
            } else if (
              formData.from &&
              fromDate &&
              !isNaN(fromDate.getTime())
            ) {
              if (toDate <= fromDate) {
                showFieldError("to", "To date must be after from date");
                isValid = false;
              }
            }
          }

          return isValid;
        };

        // UI state management - ENHANCED
        const setLoadingState = (loading) => {
          const button = document.getElementById("searchButton");
          const loadingText = button.querySelector(".loading-text");
          const loadingSpinner = button.querySelector(".loading-spinner");
          const loadingSkeleton = document.getElementById("loadingSkeleton");
          const results = document.getElementById("results");
          const emptyResults = document.getElementById("emptyResults");
          const errorMessage = document.getElementById("errorMessage");

          if (loading) {
            button.disabled = true;
            loadingText.classList.add("hidden");
            loadingSpinner.classList.remove("hidden");
            loadingSkeleton.classList.remove("hidden");
            results.classList.add("hidden");
            emptyResults.classList.add("hidden");
            errorMessage.classList.add("hidden");
          } else {
            button.disabled = false;
            loadingText.classList.remove("hidden");
            loadingSpinner.classList.add("hidden");
            loadingSkeleton.classList.add("hidden");
          }
        };

        // Error handling - ENHANCED
        const showError = (message) => {
          const errorMessage = document.getElementById("errorMessage");
          const errorText = document.getElementById("errorText");
          const results = document.getElementById("results");
          const emptyResults = document.getElementById("emptyResults");
          const loadingSkeleton = document.getElementById("loadingSkeleton");

          if (errorText && errorMessage) {
            errorText.textContent = message;
            errorMessage.classList.remove("hidden");
            results.classList.add("hidden");
            emptyResults.classList.add("hidden");
            loadingSkeleton.classList.add("hidden");
          }
        };

        const hideError = () => {
          const errorMessage = document.getElementById("errorMessage");
          if (errorMessage) {
            errorMessage.classList.add("hidden");
          }
        };

        // Data processing functions
        const processResponseData = (data) => {
          if (!data) return null;

          return {
            id: data.id || "",
            city: sanitizeText(data.city) || "Unknown",
            country: sanitizeText(data.country) || "Unknown",
            placeDetails: {
              cultures: Array.isArray(data.placeDetails?.cultures)
                ? data.placeDetails.cultures.map((item) => ({
                    name: sanitizeText(item.name) || "Unknown Culture",
                    description:
                      sanitizeText(item.description) ||
                      "No description available",
                    city: sanitizeText(item.city) || data.city,
                    category: sanitizeText(item.category) || "Cultural Site",
                    historicalPeriod:
                      sanitizeText(item.historicalPeriod) || "Historical",
                  }))
                : [],
              foods: Array.isArray(data.placeDetails?.foods)
                ? data.placeDetails.foods.map((item) => ({
                    name: sanitizeText(item.name) || "Unknown Food",
                    description:
                      sanitizeText(item.description) ||
                      "No description available",
                    city: sanitizeText(item.city) || data.city,
                    cuisineType:
                      sanitizeText(item.cuisineType) || "Local Cuisine",
                    priceRange: sanitizeText(item.priceRange) || "Moderate",
                  }))
                : [],
              touristAttractions: Array.isArray(
                data.placeDetails?.touristAttractions
              )
                ? data.placeDetails.touristAttractions.map((item) => ({
                    name: sanitizeText(item.name) || "Unknown Attraction",
                    description:
                      sanitizeText(item.description) ||
                      "No description available",
                    category: sanitizeText(item.category) || "Landmark",
                    location:
                      sanitizeText(item.location) || "Location not specified",
                  }))
                : [],
            },
            weather: Array.isArray(data.weather)
              ? data.weather.map((item) => ({
                  maxTemp: sanitizeText(item.maxTemp) || "0",
                  minTemp: sanitizeText(item.minTemp) || "0",
                  desc: sanitizeText(item.desc) || "No description",
                  day: sanitizeText(item.day) || "Unknown",
                  travelAdvice: sanitizeText(item.travelAdvice) || "",
                  feelsLike: sanitizeText(item.feelsLike) || "0°C",
                  others: sanitizeText(item.others) || "",
                }))
              : [],
            hotel: Array.isArray(data.hotel)
              ? data.hotel
                  .filter((item) => {
                    // Filter out invalid hotels
                    const name = sanitizeText(item.hotelName);
                    const price = parseNumericValue(item.price);
                    return (
                      name &&
                      name !== "N/A" &&
                      name !== "Previous" &&
                      name !== "Go to hotel" &&
                      name !== "Book Now" &&
                      price >= 0
                    );
                  })
                  .map((item) => ({
                    address:
                      sanitizeText(item.address) || "Address not available",
                    hotelName: sanitizeText(item.hotelName) || "Unknown Hotel",
                    hotelDescription:
                      sanitizeText(item.hotelDescription) ||
                      "No description available",
                    price: parseNumericValue(item.price),
                    type: sanitizeText(item.type) || "Hotel",
                    url: sanitizeText(item.url) || "#",
                    date: sanitizeText(item.date) || "",
                  }))
              : [],
          };
        };

        // Display functions
        const displayBasicInfo = (data) => {
          const displayCountry = document.getElementById("displayCountry");
          const displayCity = document.getElementById("displayCity");

          if (displayCountry) displayCountry.textContent = data.country;
          if (displayCity) displayCity.textContent = data.city;
        };

        const displayCultures = (cultures) => {
          const container = document.getElementById("cultureContent");
          if (!container) return;

          if (!cultures || cultures.length === 0) {
            container.innerHTML =
              '<div class="col-span-full text-center text-gray-500 py-8">No cultural information available</div>';
            return;
          }

          container.innerHTML = cultures
            .map(
              (culture) => `
        <div class="bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl p-6 hover:shadow-lg transition-shadow duration-200 border border-purple-100">
          <h3 class="font-bold text-lg text-gray-800 mb-3">${culture.name}</h3>
          <p class="text-gray-600 mb-3">${culture.description}</p>
          <div class="flex flex-wrap gap-2">
            <span class="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm font-medium">${culture.category}</span>
            <span class="px-3 py-1 bg-pink-100 text-pink-700 rounded-full text-sm font-medium">${culture.historicalPeriod}</span>
          </div>
        </div>
      `
            )
            .join("");
        };

        const displayFoods = (foods) => {
          const container = document.getElementById("foodsContent");
          if (!container) return;

          if (!foods || foods.length === 0) {
            container.innerHTML =
              '<div class="col-span-full text-center text-gray-500 py-8">No food information available</div>';
            return;
          }

          container.innerHTML = foods
            .map(
              (food) => `
        <div class="bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl p-6 hover:shadow-lg transition-shadow duration-200 border border-green-100">
          <h3 class="font-bold text-lg text-gray-800 mb-3">${food.name}</h3>
          <p class="text-gray-600 mb-3">${food.description}</p>
          <div class="flex flex-wrap gap-2">
            <span class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">${food.cuisineType}</span>
            <span class="px-3 py-1 bg-emerald-100 text-emerald-700 rounded-full text-sm font-medium">${food.priceRange}</span>
          </div>
        </div>
      `
            )
            .join("");
        };

        const displayAttractions = (attractions) => {
          const container = document.getElementById("attractionsContent");
          if (!container) return;

          if (!attractions || attractions.length === 0) {
            container.innerHTML =
              '<div class="col-span-full text-center text-gray-500 py-8">No attraction information available</div>';
            return;
          }

          container.innerHTML = attractions
            .map(
              (attraction) => `
        <div class="bg-gradient-to-r from-pink-50 to-rose-50 rounded-xl p-6 hover:shadow-lg transition-shadow duration-200 border border-pink-100">
          <h3 class="font-bold text-lg text-gray-800 mb-3">${attraction.name}</h3>
          <p class="text-gray-600 mb-3">${attraction.description}</p>
          <div class="flex flex-wrap gap-2">
            <span class="px-3 py-1 bg-pink-100 text-pink-700 rounded-full text-sm font-medium">${attraction.category}</span>
            <span class="px-3 py-1 bg-rose-100 text-rose-700 rounded-full text-sm font-medium">${attraction.location}</span>
          </div>
        </div>
      `
            )
            .join("");
        };

        const displayWeather = (weather) => {
          const container = document.getElementById("weatherContent");
          if (!container) return;

          if (!weather || weather.length === 0) {
            container.innerHTML =
              '<div class="col-span-full text-center text-gray-500 py-8">No weather information available</div>';
            return;
          }

          container.innerHTML = weather
            .map(
              (day) => `
        <div class="bg-gradient-to-r from-orange-50 to-yellow-50 rounded-xl p-6 hover:shadow-lg transition-shadow duration-200 border border-orange-100">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-bold text-lg text-gray-800">${day.day}</h3>
            <div class="text-right">
              <span class="text-2xl font-bold text-orange-600">${
                day.maxTemp
              }°</span>
              <span class="text-sm text-gray-500">/${day.minTemp}°</span>
            </div>
          </div>
          <p class="text-gray-600 mb-2">${day.desc}</p>
          <p class="text-sm text-gray-500 mb-2">Feels like: ${day.feelsLike}</p>
          ${
            day.travelAdvice
              ? `<p class="text-sm text-blue-600 font-medium">💡 ${day.travelAdvice}</p>`
              : ""
          }
          ${
            day.others
              ? `<p class="text-xs text-gray-400 mt-2">${day.others}</p>`
              : ""
          }
        </div>
      `
            )
            .join("");
        };

        const displayHotels = (hotels) => {
          const container = document.getElementById("hotelsContent");
          if (!container) return;

          if (!hotels || hotels.length === 0) {
            container.innerHTML =
              '<div class="col-span-full text-center text-gray-500 py-8">No hotel information available</div>';
            return;
          }

          container.innerHTML = hotels
            .map(
              (hotel) => `
        <div class="bg-gradient-to-r from-indigo-50 to-blue-50 rounded-xl p-6 hover:shadow-lg transition-shadow duration-200 border border-indigo-100">
          <h3 class="font-bold text-lg text-gray-800 mb-2">${
            hotel.hotelName
          }</h3>
          <p class="text-gray-600 mb-3">${hotel.hotelDescription}</p>
          <div class="text-2xl font-bold text-indigo-600 mb-2">
            ₦${hotel.price.toLocaleString()}
          </div>
          <div class="flex flex-wrap gap-2 mb-3">
            <span class="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm font-medium">${
              hotel.type
            }</span>
          </div>
          <div class="text-sm text-gray-500 mb-3">${hotel.address}</div>
          ${
            hotel.url && hotel.url !== "#"
              ? `
            <a href="${hotel.url}" target="_blank" rel="noopener noreferrer" 
               class="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors duration-200 text-sm font-medium">
              View Details
            </a>
          `
              : ""
          }
        </div>
      `
            )
            .join("");
        };

        // Sorting functions
        const sortHotels = (sortType) => {
          if (!currentData?.hotel) return;

          const sorted = [...currentData.hotel].sort((a, b) => {
            if (sortType === "price-asc") {
              return a.price - b.price;
            } else if (sortType === "price-desc") {
              return b.price - a.price;
            }
            return 0;
          });

          currentData.hotel = sorted;
          displayHotels(sorted);
        };

        const sortWeather = (sortType) => {
          if (!currentData?.weather) return;

          const sorted = [...currentData.weather].sort((a, b) => {
            if (sortType === "temp") {
              return (
                parseNumericValue(b.maxTemp) - parseNumericValue(a.maxTemp)
              );
            }
            return 0;
          });

          currentData.weather = sorted;
          displayWeather(sorted);
        };

        const sortByDescription = (section) => {
          if (!currentData?.placeDetails) return;

          let data, displayFunc;

          switch (section) {
            case "culture":
              data = currentData.placeDetails.cultures;
              displayFunc = displayCultures;
              break;
            case "food":
              data = currentData.placeDetails.foods;
              displayFunc = displayFoods;
              break;
            case "attractions":
              data = currentData.placeDetails.touristAttractions;
              displayFunc = displayAttractions;
              break;
            default:
              return;
          }

          if (!data) return;

          const currentState = sortStates[section];
          let sorted;

          if (currentState === "default" || currentState === "desc") {
            // Sort A-Z
            sorted = [...data].sort((a, b) => a.name.localeCompare(b.name));
            sortStates[section] = "asc";
          } else {
            // Sort Z-A
            sorted = [...data].sort((a, b) => b.name.localeCompare(a.name));
            sortStates[section] = "desc";
          }

          if (section === "culture") {
            currentData.placeDetails.cultures = sorted;
          } else if (section === "food") {
            currentData.placeDetails.foods = sorted;
          } else if (section === "attractions") {
            currentData.placeDetails.touristAttractions = sorted;
          }

          displayFunc(sorted);
        };

        // Main search function using axios
        const performSearch = async (formData) => {
          try {
            setLoadingState(true);
            hideError();

            // Your API endpoint
            const baseUrl = "http://localhost:8080/api/agg/find";

            // Build query parameters
            const params = new URLSearchParams({
              city: formData.city.trim(),
              country: formData.country.trim(),
            });

            // Add optional date parameters if provided
            if (formData.from) {
              const fromDate = new Date(formData.from);
              params.append("from", fromDate.getDate().toString());
            }

            if (formData.to) {
              const toDate = new Date(formData.to);
              params.append("to", toDate.getDate().toString());
            }

            const apiUrl = `${baseUrl}?${params.toString()}`;

            // Using axios for the GET API call
            const response = await axios.get(apiUrl, {
              timeout: 30000,
              headers: {
                Accept: "application/json",
              },
            });

            if (response.status !== 200) {
              throw new Error(
                `Server responded with status: ${response.status}`
              );
            }

            const data = response.data;

            if (!data) {
              throw new Error("No data received from server");
            }

            // Process and store the data
            originalData = processResponseData(data);
            currentData = JSON.parse(JSON.stringify(originalData)); // Deep copy

            // Check if we have any meaningful data
            const hasData =
              currentData.placeDetails.cultures.length > 0 ||
              currentData.placeDetails.foods.length > 0 ||
              currentData.placeDetails.touristAttractions.length > 0 ||
              currentData.weather.length > 0 ||
              currentData.hotel.length > 0;

            if (!hasData) {
              document
                .getElementById("emptyResults")
                .classList.remove("hidden");
              document.getElementById("results").classList.add("hidden");
              return;
            }

            // Display all data
            displayBasicInfo(currentData);
            displayCultures(currentData.placeDetails.cultures);
            displayFoods(currentData.placeDetails.foods);
            displayAttractions(currentData.placeDetails.touristAttractions);
            displayWeather(currentData.weather);
            displayHotels(currentData.hotel);

            // Show results
            document.getElementById("results").classList.remove("hidden");
            document.getElementById("emptyResults").classList.add("hidden");
          } catch (error) {
            console.error("Search error:", error);

            let errorMessage = "Failed to fetch travel information. ";

            if (
              error.code === "ECONNABORTED" ||
              error.message.includes("timeout")
            ) {
              errorMessage += "Request timed out. Please try again.";
            } else if (error.response) {
              errorMessage += `Server error: ${error.response.status} - ${error.response.statusText}`;
            } else if (error.request) {
              errorMessage +=
                "Network error. Please check your connection and ensure the API server is running.";
            } else if (error.message.includes("localhost")) {
              errorMessage +=
                "Cannot connect to localhost API. Make sure your server is running on http://localhost:8080";
            } else {
              errorMessage += error.message;
            }

            showError(errorMessage);
          } finally {
            setLoadingState(false);
          }
        };

        // Form submission handler
        const handleFormSubmit = async (event) => {
          event.preventDefault();

          const formData = new FormData(event.target);
          const searchData = {
            country: formData.get("country"),
            city: formData.get("city"),
            from: formData.get("from"),
            to: formData.get("to"),
          };

          // Validate form data
          if (!validateForm(searchData)) {
            return;
          }

          // Perform search
          await performSearch(searchData);
        };

        // Initialize the application
        const init = () => {
          // Set up form submission
          const searchForm = document.getElementById("searchForm");
          if (searchForm) {
            searchForm.addEventListener("submit", handleFormSubmit);
          }

          // Initialize dates
          initializeDates();

          // Add input event listeners for real-time validation feedback
          ["country", "city", "from", "to"].forEach((fieldName) => {
            const field = document.getElementById(fieldName);
            if (field) {
              field.addEventListener("input", () => hideFieldError(fieldName));
              field.addEventListener("change", () => hideFieldError(fieldName));
            }
          });

          // Add date field dependencies
          const fromField = document.getElementById("from");
          const toField = document.getElementById("to");

          if (fromField && toField) {
            fromField.addEventListener("change", () => {
              const fromDate = new Date(fromField.value);
              if (!isNaN(fromDate.getTime())) {
                const nextDay = new Date(fromDate);
                nextDay.setDate(fromDate.getDate() + 1);
                toField.min = formatDate(nextDay);

                // If current to date is before new minimum, update it
                const currentToDate = new Date(toField.value);
                if (
                  !isNaN(currentToDate.getTime()) &&
                  currentToDate <= fromDate
                ) {
                  toField.value = formatDate(nextDay);
                }
              }
            });
          }
        };

        // Public API
        return {
          init,
          sortHotels,
          sortWeather,
          sortByDescription,
          performSearch,
        };
      })();

      // Initialize when DOM is loaded
      document.addEventListener("DOMContentLoaded", () => {
        window.TravelDashboard.init();
      });
    