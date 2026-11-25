import React from 'react'
import { Link } from 'react-router'

function LibraryHeader() {
    const libHeader = "Library Management System"
    return (
        <div>
            <header>
                {/*<h1>{libHeader}</h1>*/}
                <Link to="/books" style={{paddingRight:"10px"}}>BOOKS</Link>
                <Link to="/office" style={{paddingRight:"10px"}}>OFFICE</Link>
                <Link to="/available-offices" style={{paddingRight:"10px"}}>AVAILABLE OFFICES</Link>
                <Link to="/" style={{paddingRight:"10px"}}>LOGOUT</Link>
                <p>This is a libary system that will help you to manage books.</p>
            </header>
        </div>
    )
}

export default LibraryHeader