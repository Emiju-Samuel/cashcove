export const baseUrl = "http://localhost:8080/api/v1.0";

const CLOUDINARY_NAME = "dktaopmnb";

export const apiEndpoints = {
    LOGIN: "/login",
    REGISTER: "/register",
    GET_USER_INFO: "/profile",
    UPLOAD_IMAGE: `https://api.cloudinary.com/v1_1/${CLOUDINARY_NAME}/image/upload`,
}