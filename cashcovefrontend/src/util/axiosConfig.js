import axios from "axios";
import { baseUrl } from "./apiEndpoints";

const axiosConfig = axios.create({
    baseURL: baseUrl,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json"
    }
});

// list of endpoints that do not require authorization header
const excludeEndpoints = ["/login", "/register", "/status", "/activate", "/health"];

// request interceptors
axiosConfig.interceptors.request.use((config)=>{
    const shouldSkipToken = excludeEndpoints.some((endpoint)=>{
        return config.url?.includes(endpoint)
    });

    if(!shouldSkipToken){
        const accessToken = localStorage.getItem("token");
        if(accessToken){
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// response interceptor
axiosConfig.interceptors.response.use((response)=> {
    return response;
}, (error)=> {
    if(error.response){
        if(error.response.status === 401){
            window.location.href = "/login";
        }else if(error.response.status === 500){
            console.error("Server error, pls try again later");
        }
    } else if(error.code === "ECONNABORTED"){
        console.error("Request timeout. Please try again");
    } else if(error.code === "ERR_NETWORK"){
        console.error("Network error: Cannot reach the server. Please ensure the backend is running.");
    }
    return Promise.reject(error);
});

export default axiosConfig;