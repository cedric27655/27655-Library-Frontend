import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import LibraryButton from './LibraryButton'

function LibraryLogin() {
    const navigate = useNavigate()
    const [doYouHaveAnAccount, setDoYouHaveAnAccount] = useState(true);

    const [userName, setUserName] = useState("")
    const [password, setPassword] = useState("")

    function navigateBetween() {
        setDoYouHaveAnAccount(!doYouHaveAnAccount)
    }

    function loginAction() {
        // Navigate directly to the Available Offices assignment page
        navigate('/available-offices')
    }

    return (
        <div style={{ textAlign: 'center', marginTop: '100px' }}>
            {doYouHaveAnAccount ? (
                <div style={{ border: "3px solid blue", padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
                    <div style={{ marginBottom: '10px' }}>
                        <label>UserName: </label>
                        <input type="text" onChange={e => setUserName(e.target.value)} placeholder='Enter Your username' />
                    </div>
                    <div style={{ marginBottom: '10px' }}>
                        <label>Password: </label>
                        <input type="password" onChange={e => setPassword(e.target.value)} />
                    </div>
                    <LibraryButton buttonLabel="LOGIN" onClickButton={loginAction} />
                    <div style={{ marginTop: '15px' }}>
                        <label>New here? </label> <LibraryButton buttonLabel="Create Account" onClickButton={navigateBetween} />
                    </div>
                    <div style={{ marginTop: '20px', borderTop: '1px solid #eee', paddingTop: '10px' }}>
                        <Link to="/available-offices" style={{ color: '#007bff', fontWeight: 'bold' }}>
                            View Available Offices &rarr;
                        </Link>
                    </div>
                </div>
            ) : (
                <div style={{ border: "3px solid indigo", padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
                    <div style={{ marginBottom: '10px' }}>
                        <label>Student ID: </label>
                        <input type="text" placeholder='Enter Your Student ID' />
                    </div>
                    <div style={{ marginBottom: '10px' }}>
                        <label>Email: </label>
                        <input type="email" />
                    </div>
                    <div style={{ marginBottom: '10px' }}>
                        <label>Password: </label>
                        <input type="password" />
                    </div>
                    <div style={{ marginBottom: '10px' }}>
                        <label>Confirm Password: </label>
                        <input type="password" />
                    </div>
                    <LibraryButton buttonLabel="LOGIN" onClickButton={loginAction} />
                    <div style={{ marginTop: '15px' }}>
                        <label>Already have an account?? </label> <LibraryButton buttonLabel="Sign In" onClickButton={navigateBetween} />
                    </div>
                </div>
            )}
        </div>
    )
}

export default LibraryLogin
