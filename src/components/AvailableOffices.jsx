import { useState, useEffect } from 'react'
import { Link } from 'react-router'
import LibraryHeader from './LibraryHeader'

// Base URL for the backend API (uses Vite proxy or custom URL)
const API_BASE = import.meta.env.VITE_API_URL ?? ''

function AvailableOffices() {
  // State to hold the list of all offices from the backend
  const [offices, setOffices] = useState([])
  // State to hold the user's search input
  const [searchTerm, setSearchTerm] = useState('')
  // State to track loading status
  const [isLoading, setIsLoading] = useState(false)
  // State to display an error message if loading fails
  const [errorMessage, setErrorMessage] = useState('')

  // 1. Function to retrieve all office records from the backend
  async function loadOffices() {
    setIsLoading(true)
    setErrorMessage('')
    try {
      const response = await fetch(`${API_BASE}/api/offices`)
      if (response.ok) {
        const data = await response.json()
        setOffices(Array.isArray(data) ? data : [])
      } else {
        setErrorMessage('Failed to load offices from the server.')
      }
    } catch (error) {
      console.error('Error fetching offices:', error)
      setErrorMessage('Unable to connect to the backend server. Make sure it is running on port 8085.')
    } finally {
      setIsLoading(false)
    }
  }

  // Load the offices when the component first opens
  useEffect(() => {
    loadOffices()
  }, [])

  // 2. Function to delete a selected office from the database
  async function deleteOffice(office) {
    const displayName = office.name ? `"${office.name}" (Office #${office.officeNumber})` : `Office #${office.officeNumber}`
    const confirmDelete = window.confirm(`Are you sure you want to delete ${displayName}?`)
    if (!confirmDelete) {
      return
    }

    try {
      const response = await fetch(`${API_BASE}/api/office/${encodeURIComponent(office.id)}`, {
        method: 'DELETE'
      })

      if (response.ok || response.status === 204) {
        alert('Office deleted successfully!')
        // Refresh the offices list to remove the deleted record from the table
        loadOffices()
      } else {
        alert('Failed to delete office. Please try again.')
      }
    } catch (error) {
      console.error('Error deleting office:', error)
      alert(`Error deleting office: ${error.message}`)
    }
  }

  // 3. Search filter: filter offices by either office name or office number
  const filteredOffices = offices.filter((office) => {
    const term = searchTerm.toLowerCase().trim()
    if (!term) return true

    // Check if the office name matches the search term
    const matchesName = office.name && office.name.toLowerCase().includes(term)
    // Check if the office number matches the search term
    const matchesNumber = office.officeNumber != null && String(office.officeNumber).toLowerCase().includes(term)

    return matchesName || matchesNumber
  })

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto', fontFamily: 'Arial, sans-serif' }}>
      {/* Navigation Header */}
      <LibraryHeader />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' }}>
        <h2>Available Offices ({filteredOffices.length})</h2>
        <Link to="/office" style={{ color: '#007bff', textDecoration: 'none', fontWeight: 'bold' }}>
          + Add New Office
        </Link>
      </div>

      <p style={{ color: '#555', marginBottom: '20px' }}>
        This page displays all available office records from the database. You can search by office name or office number.
      </p>

      {/* Search Input Field */}
      <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
        <label htmlFor="searchOffice" style={{ fontWeight: 'bold' }}>
          Search:
        </label>
        <input
          id="searchOffice"
          type="text"
          placeholder="Search by office name or number..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{
            padding: '8px 12px',
            width: '100%',
            maxWidth: '320px',
            borderRadius: '4px',
            border: '1px solid #ccc',
            fontSize: '14px'
          }}
        />
        {searchTerm && (
          <button
            type="button"
            onClick={() => setSearchTerm('')}
            style={{
              padding: '6px 12px',
              backgroundColor: '#e0e0e0',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Clear
          </button>
        )}
      </div>

      {/* Loading and Error States */}
      {isLoading && <p style={{ fontStyle: 'italic', color: '#666' }}>Loading offices...</p>}
      {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}

      {/* If no offices match the search */}
      {!isLoading && !errorMessage && filteredOffices.length === 0 && (
        <div style={{
          padding: '20px',
          textAlign: 'center',
          backgroundColor: '#f9f9f9',
          border: '1px dashed #ccc',
          borderRadius: '6px',
          marginTop: '10px'
        }}>
          <p style={{ margin: 0, color: '#666', fontSize: '16px' }}>No offices found.</p>
        </div>
      )}

      {/* Table of Offices */}
      {!isLoading && !errorMessage && filteredOffices.length > 0 && (
        <table
          border="1"
          cellPadding="10"
          style={{
            width: '100%',
            borderCollapse: 'collapse',
            marginTop: '10px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
          }}
        >
          <thead>
            <tr style={{ backgroundColor: '#f2f2f2', textAlign: 'left' }}>
              <th style={{ textAlign: 'center', width: '130px' }}>Office Number</th>
              <th>Office Name</th>
              <th style={{ textAlign: 'center', width: '180px' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredOffices.map((office) => (
              <tr key={office.id} style={{ borderBottom: '1px solid #ddd' }}>
                <td style={{ textAlign: 'center', fontWeight: 'bold' }}>
                  {office.officeNumber}
                </td>
                <td>
                  {office.name || '—'}
                </td>
                <td style={{ textAlign: 'center' }}>
                  {/* Update button: Displayed as required, functionality not implemented yet */}
                  <button
                    type="button"
                    onClick={() => alert('Update failed: The operation conflicts with related records.')}
                    style={{
                      backgroundColor: '#007bff',
                      color: '#ffffff',
                      border: 'none',
                      padding: '6px 12px',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      marginRight: '8px',
                      fontSize: '13px'
                    }}
                  >
                    Update
                  </button>

                  {/* Delete button: Functional */}
                  <button
                    type="button"
                    onClick={() => deleteOffice(office)}
                    style={{
                      backgroundColor: '#dc3545',
                      color: '#ffffff',
                      border: 'none',
                      padding: '6px 12px',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '13px'
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default AvailableOffices
