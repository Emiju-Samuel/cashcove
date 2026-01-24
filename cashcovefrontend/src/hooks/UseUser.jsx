import { useContext, useEffect } from "react"
import { useNavigate } from "react-router-dom";
import {AppContext} from "../context/AppContext.jsx"
import { apiEndpoints } from "../util/apiEndpoints.js";
import axiosConfig from "../util/axiosConfig.js";

export const UseUser = () => {
    const {user, setUser, clearUser} = useContext(AppContext);
    const navigate = useNavigate();

    useEffect(()=>{
        if(user){
            return;
        }

        let isMounted = true;

        const fetchUserInfo = async () => {
            try{
                const response = await axiosConfig.get(apiEndpoints.GET_USER_INFO);

                if(isMounted && response.data){
                    setUser(response.data);
                }
            }catch(error){
                console.log("Failed to fetch the user info", error);
                if(isMounted){
                    clearUser();
                    navigate("/login");
                }
            }
        }

        fetchUserInfo();

        return () => {
            isMounted = false;
        }
    }, [setUser, clearUser, navigate]);
}