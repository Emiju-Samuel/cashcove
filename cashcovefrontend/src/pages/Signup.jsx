import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom';
import { LoaderCircle } from 'lucide-react';
import { toast } from 'react-toastify';
import { assets } from '../assets/assets';
import Input from '../Components/Input';
import { validateEmail } from '../util/Validation';
import axiosConfig from '../util/axiosConfig';
import { apiEndpoints } from '../util/apiEndpoints';
import ProfilePhotoSelector from '../Components/ProfilePhotoSelector';
import uploadProfileImage from '../util/uploadProfileImage';

const Signup = () => {


    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [image, setImage] = useState(null);

    const navigate = useNavigate();

    const handleSubmit = async(e)=>{
        e.preventDefault();
        let profileImageUrl = "";
        setIsLoading(true);

        // basic validation
        if(!fullName.trim()){
            setError("Please enter your full name");
            setIsLoading(false);
            return;
        }
        if(!validateEmail(email)){
            setError("Please enter your email address");
            setIsLoading(false);
            return;
        }
        if(!password.trim()){
            setError("Please enter your password");
            setIsLoading(false);
            return;
        }
        setError("");

        // signup API call
        try{
            // upload profile image if selected
            if(image){
                const imageUrl = await uploadProfileImage(image);
                profileImageUrl = imageUrl || "";
            }
            const response = await axiosConfig.post(apiEndpoints.REGISTER, {
                fullName,
                email,
                password,
                profileImageUrl
            })
            if(response.status === 201){
                toast.success("Profile created successfully.");
                navigate("/login");
            }
        }catch(err){
            console.error('Something went wrong', err);
            setError(err.response?.data?.message || "An error occurred during registration. Please try again.");
        }finally{
            setIsLoading(false);
        }
    }

  return (
    <div className="h-screen 2-full relative flex items-center justify-center overflow-hidden">
        {/* background image with blur */}
        <img src ={assets.loginBg} alt='Background' className='absolute inset-0 w-full h-full object-cover filter blur-sm' />

        <div className="relative z-10 w-full max-w-lg px-6">
            <div className="bg-white bg-opacity-95 backdrop-blur-sm rounded-lg shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
                <h3 className="text-2xl font-semibold text-black text-center mb-2">
                    Create an Account
                </h3>
                <p className='text-sm text-slate-700 text-center mb-8'>
                    Start monitoring your spendings by joining CashCove.
                </p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="flex justify-center mb-6">
                        <ProfilePhotoSelector image={image} setImage={setImage}/>
                    </div>
                    <div className='grid grid-cols-2 md:grid-cols-2 gap-4'>
                        <Input
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        label="Full Name"
                        placeholder="Enter full name"
                        type="text"
                        />

                        <Input
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        label="Email address"
                        placeholder="xxxxxx@example.com"
                        type="text"
                        />

                        <div className="col-span-2">
                            <Input
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        label="Password"
                        placeholder="*********"
                        type="password"
                        />
                        </div>
                    </div>
                    {error && (
                        <p className="text-red-800 text-sm text-center bg-red-50 p-2 rounded">
                            {error}
                        </p>
                    )}

                    <button disabled={isLoading} className={`btn-primary w-full py-3 text-lg font-medium flex items-center justify-center gap-2 ${isLoading ? 'opacity-60 cursor-not-allowed' : ''}`} type='submit'>
                        {isLoading ? (
                            <>
                            <LoaderCircle className="animate-spin w-5 h-5" />
                            Signing Up...
                            </>
                        ): (
                            "Sign up"
                        )}
                    </button>

                    <p className="text-sm text-slate-800 text-center mt-6">
                        Already have an Account?
                        <Link to="/login" className='font-medium text-primary underline hover:text-primary-dark transition-colors'>Login</Link>
                    </p>

                </form>
            </div>

        </div>
    </div>
  )
}

export default Signup