const axios = require('axios');

/**
 * Geocode using OpenCage Geocoder API
 * @param {string} address - The address to geocode
 * @param {string} apiKey - OpenCage API key
 * @returns {Promise<Object>} - { latitude, longitude, formattedAddress }
 */
const geocodeAddressOpenCage = async (address, apiKey) => {
  try {
    const response = await axios.get('https://api.opencagedata.com/geocode/v1/json', {
      params: {
        q: address,
        key: apiKey,
        countrycode: 'vn', // Limit to Vietnam
        language: 'vi',
        limit: 1
      }
    });

    if (!response.data || !response.data.results || response.data.results.length === 0) {
      throw new Error('Không tìm thấy địa chỉ. Vui lòng kiểm tra lại địa chỉ của bạn.');
    }

    const result = response.data.results[0];
    const { lat, lng } = result.geometry;

    return {
      latitude: lat,
      longitude: lng,
      formattedAddress: result.formatted
    };
  } catch (error) {
    if (error.response) {
      throw new Error(`Lỗi từ OpenCage API: ${error.response.status}`);
    }
    throw error;
  }
};

/**
 * Reverse geocode using OpenCage API
 * @param {number} latitude - Latitude
 * @param {number} longitude - Longitude
 * @param {string} apiKey - OpenCage API key
 * @returns {Promise<Object>} - { formattedAddress, addressComponents }
 */
const reverseGeocodeOpenCage = async (latitude, longitude, apiKey) => {
  try {
    const response = await axios.get('https://api.opencagedata.com/geocode/v1/json', {
      params: {
        q: `${latitude},${longitude}`,
        key: apiKey,
        language: 'vi'
      }
    });

    if (!response.data || !response.data.results || response.data.results.length === 0) {
      throw new Error('Không tìm thấy địa chỉ cho tọa độ này.');
    }

    const result = response.data.results[0];
    const components = result.components || {};

    return {
      formattedAddress: result.formatted,
      addressComponents: {
        street: components.road || components.street || '',
        houseNumber: components.house_number || '',
        district: components.suburb || components.district || '',
        city: components.city || components.town || components.village || '',
        province: components.state || components.county || '',
        country: components.country || 'Vietnam'
      }
    };
  } catch (error) {
    if (error.response) {
      throw new Error(`Lỗi từ OpenCage API: ${error.response.status}`);
    }
    throw error;
  }
};

/**
 * Main geocoding function - Chỉ dùng OpenCage
 * @param {string} address - The address to geocode
 * @returns {Promise<Object>} - { latitude, longitude, formattedAddress }
 */
const geocodeAddress = async (address) => {
  if (!process.env.OPENCAGE_API_KEY) {
    throw new Error('OpenCage API key chưa được cấu hình. Vui lòng thêm OPENCAGE_API_KEY vào file .env');
  }

  return await geocodeAddressOpenCage(address, process.env.OPENCAGE_API_KEY);
};

/**
 * Main reverse geocoding function - Chỉ dùng OpenCage
 * @param {number} latitude - Latitude
 * @param {number} longitude - Longitude
 * @returns {Promise<Object>} - { formattedAddress, addressComponents }
 */
const reverseGeocode = async (latitude, longitude) => {
  if (!process.env.OPENCAGE_API_KEY) {
    throw new Error('OpenCage API key chưa được cấu hình. Vui lòng thêm OPENCAGE_API_KEY vào file .env');
  }

  return await reverseGeocodeOpenCage(latitude, longitude, process.env.OPENCAGE_API_KEY);
};

module.exports = {
  geocodeAddress,
  reverseGeocode
};
