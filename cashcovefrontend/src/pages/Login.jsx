import React, { useContext, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Input from "../Components/Input";
import { assets } from "../assets/assets";
import axiosConfig from "../util/axiosConfig";
import { apiEndpoints } from "../util/apiEndpoints";
import { AppContext } from "../context/AppContext";
import { LoaderCircle } from "lucide-react";
import { validateEmail } from "../util/Validation";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(null);
  const {setUser} = useContext(AppContext);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    // basic validation
    if (!validateEmail(email)) {
      setError("Please enter your email address");
      setIsLoading(false);
      return;
    }
    if (!password.trim()) {
      setError("Please enter your password");
      setIsLoading(false);
      return;
    }
    setError("");

    // Login API call
    try{
      const response = await axiosConfig.post(apiEndpoints.LOGIN, {
        email,
        password,
      });
      const {token, user} = response.data;
      if(token){
        localStorage.setItem("token", token);
        setUser(user);
        navigate("/dashboard");
      }
    }catch(err){
      if(err.response && err.response.data.message){
        setError(err.response.data.message)
      }
      console.error("Something went wrong while logging in", err);
    }finally{
      setIsLoading(false);
    }
  };



  return (
    <div className="h-screen 2-full relative flex items-center justify-center overflow-hidden">
      {/* background image with blur */}
      <img
        src={assets.loginBg}
        alt="Background"
        className="absolute inset-0 w-full h-full object-cover filter blur-sm"
      />

      <div className="relative z-10 w-full max-w-lg px-6">
        <div className="bg-white bg-opacity-95 backdrop-blur-sm rounded-lg shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
          <h3 className="text-2xl font-semibold text-black text-center mb-2">
            Login to CashCove
          </h3>
          <p className="text-sm text-slate-700 text-center mb-8">
            Enter your login details
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              label="Email address"
              placeholder="xxxxxx@example.com"
              type="text"
            />

            <Input
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              label="Password"
              placeholder="*********"
              type="password"
            />

            <div className="d-flex justify-content-between mb-3">
                    <Link to="/reset-password" className='text-decoration-none'>Forgot Password?</Link>
                </div>

            {error && (
              <p className="text-red-800 text-sm text-center bg-red-50 p-2 rounded">
                {error}
              </p>
            )}

            <button
            disabled={isLoading}
              className={`btn-primary w-full py-3 text-lg font-medium flex items-center justify-center gap-2 ${isLoading ? 'opacity-60 cursor-not-allowed': ''}`}
              type="submit"
            >
              {isLoading ? (
                <>
                <LoaderCircle className="animate-spin w-4 h-4"/>
                Logging in...
                </>
              ): (
                "Login"
              )}
            </button>

            <p className="text-sm text-slate-800 text-center mt-6">
              Don't have an account?
              <Link
                to="/signup"
                className="font-medium text-primary underline hover:text-primary-dark transition-colors"
              >
                Signup
              </Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
