import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom';
import { assets } from '../assets/assets';
import Input from '../Components/Input';

const Signup = () => {


    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const navigate = useNavigate();

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

                <form className="space-y-4">
                    <div className="flex justify-center mb-6">
                        {/* Profile Image */}
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

                    <button className="btn-primary w-full py-3 text-lg font-medium" type='submit'>
                        Sign Up
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