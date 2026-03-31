import React, { useContext, useRef, useState } from 'react'
import { AppContext } from '../context/AppContext';
import { Link, useNavigate } from 'react-router-dom';
import { apiEndpoints, baseUrl } from '../util/apiEndpoints';
import axios from 'axios';
import { assets } from '../assets/assets';
import axiosConfig from '../util/axiosConfig';
import { toast } from 'react-toastify';

const ResetPassword = () => {

  const inputRef = useRef([]);
  const [loading, setLoading] = useState(false);
  const {getUserData, isLoggedIn, userData, backendUrl} = useContext(AppContext);
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [isEmailSent, setIsEmailSent] = useState(false);
  const [otp, setOtp] = useState("");
  const [isOtpSubmitted, setIsOtpSubmitted] = useState(false);


  axios.defaults.withCredentials = true;

  const handleChange = (e, index) => {
    const value = e.target.value.replace(/\D/, "");
    e.target.value = value;
    if(value && index < 5){
    inputRef.current[index + 1].focus();
    }
  }

  const handleKeyDown = (e, index) => {
    if(e.key === "Backspace" && !e.target.value && index > 0){
      inputRef.current[index - 1].focus();
    }
  }

  const handlePaste = (e) => {
    e.preventDefault();
    const paste = e.clipboardData.getData("text").slice(0, 6).split("");
    paste.forEach((digit, i) => {
      if(inputRef.current[i]){
        inputRef.current[i].value = digit;
      }
    });
    const next = paste.length < 6 ? paste.length : 5;
    inputRef.current[next].focus();
  }


  const onSubmitEmail = async (e) => {
    e.preventDefault();
    setLoading(true);
    try{
      const response = await axiosConfig.post(`${apiEndpoints.SEND_RESET_OTP}?email=${encodeURIComponent(email)}`);
      if(response.status === 200){
        toast.success("Password reset OTP sent successfully");
        setIsEmailSent(true);
      }else{
        toast.error("Something went wrong, please try again");
      }
    }catch(error){
      console.error(error.message);
    }finally{
      setLoading(false);
    }
  }

  const handleVerify = () => {
    const otp = inputRef.current.map((input) => input.value).join("");
    if(otp.length !== 6){
      toast.error("Please enter all 6 digits of the otp");
      return;
    }

    setOtp(otp);
    setIsOtpSubmitted(true);
  }

  const onSubmitNewPassword = async (e) => {
    e.preventDefault();
    setLoading(true);
    try{
      const response = await axiosConfig.post(apiEndpoints.RESET_PASSWORD, {email, otp, newPassword});
      if(response.status === 200){
        toast.success("Password reset successfully");
        navigate("/login")
      }else{
        toast.error("Failed to reset password");
      }
    }catch(error){
      console.error(error.message);
      toast.error(error.response?.data?.message || "Error resetting password");
    }finally{
      setLoading(false);
    }
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-r from-blue-500 to-blue-400">
      <Link to="/" className='absolute top-0 left-0 p-4 flex items-center gap-2 text-decoration-none'>
        <img src={assets.logo} alt="logo" height={32} width={32} />
        <span className="text-xl font-semibold text-white">CashCove</span>
      </Link>

      {/* Reset password card */}
      {!isEmailSent && (
        <div className="rounded-3xl p-8 text-center bg-white w-full max-w-md shadow-lg">
          <h4 className='mb-2 text-2xl font-bold'>Reset Password</h4>
          <p className="mb-6 text-gray-600">Enter your registered email address</p>
          <form onSubmit={onSubmitEmail}>
            <div className="flex items-center mb-6 bg-gray-100 rounded-full px-4 py-3">
              <i className="bi bi-envelope text-gray-400 mr-3"></i>
              <input 
                type="email" 
                className="bg-transparent border-0 w-full outline-none text-gray-800 placeholder-gray-400" 
                placeholder='Enter email address'
                onChange={(e) => setEmail(e.target.value)}
                value={email}
                required
              />
            </div>
            <button className="btn btn-primary w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition" type='submit'>
              Submit
            </button>
          </form>
        </div>
      )}

      {/* OTP cards */}
      {!isOtpSubmitted && isEmailSent && (
        <div className="p-8 rounded-3xl shadow-lg bg-white w-full max-w-md">
          <h4 className='text-center font-bold text-2xl mb-2'>Email Verify OTP</h4>
          <p className="text-center text-gray-600 mb-6">
            Enter the 6-digit code sent to your email.
          </p>

          <div className="flex justify-center gap-3 mb-6">
            {[...Array(6)].map((_, i) => (
              <input
                key={i}
                type='text'
                maxLength={1}
                className='w-12 h-12 text-center text-xl font-semibold border-2 border-gray-300 rounded-lg focus:outline-none focus:border-blue-600'
                ref={(el) => (inputRef.current[i] = el)}
                onChange={(e) => handleChange(e, i)}
                onKeyDown={(e) => handleKeyDown(e, i)}
                onPaste={handlePaste}
              />
            ))}
          </div>

          <button className="btn btn-primary w-full font-semibold py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition disabled:opacity-50" disabled={loading} onClick={handleVerify}>
            {loading ? "Verifying..." : "Verify email"}
          </button>
        </div>
      )}

      {/* New password form */}
      {isOtpSubmitted && isEmailSent && (
        <div className="rounded-3xl p-8 text-center bg-white w-full max-w-md shadow-lg">
          <h4 className="text-2xl font-bold mb-2">New Password</h4>
          <p className="mb-6 text-gray-600">Enter the new password below</p>
          <form onSubmit={onSubmitNewPassword}>
            <div className="flex items-center mb-6 bg-gray-100 rounded-full px-4 py-3">
              <i className="bi bi-person-fill-lock text-gray-400 mr-3"></i>
              <input 
                type="password" 
                className='bg-transparent border-0 w-full outline-none text-gray-800 placeholder-gray-400'
                placeholder='•••••••••'
                onChange={(e) => setNewPassword(e.target.value)}
                value={newPassword}
                required
              />
            </div>
            <button type='submit' className='btn btn-primary w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition disabled:opacity-50' disabled={loading}>
              {loading ? "Loading..." : "Submit"}
            </button>
          </form>
        </div>
      )}
    </div>
  )
}

export default ResetPassword