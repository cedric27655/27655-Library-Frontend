import { useState, useEffect } from 'react'
import LibraryButton from './LibraryButton'
import LibraryHeader from './LibraryHeader';

const API_BASE = import.meta.env.VITE_API_URL ?? ''

async function parseErrorMessage(response, fallbackMsg) {
  try {
    const errorData = await response.json()
    if (errorData && errorData.message) {
      return errorData.message
    }
  } catch {
     
  }
  try {
    const text = await response.text()
    if (text) return text
  } catch {
   
  }
  return fallbackMsg || response.statusText || 'Unknown error'
}
function Office() {
  const [officeName, setOfficeName] = useState('')
  const [officeNumber, setOfficeNumber] = useState('')
  const [offNumber, setOffNumber] = useState('')
  const [officesList, setOfficesList] = useState([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function loadOffices() {
    setIsLoading(true)
    try {
      const res = await fetch(`${API_BASE}/api/offices`)
      if (res.ok) {
        const data = await res.json()
        setOfficesList(Array.isArray(data) ? data : [])
      }
    } catch (err) {
      console.error('Failed to load offices:', err)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadOffices()
  }, [])

  async function saveOffice() {
    const trimmedName = officeName.trim()
    const parsedOfficeNumber = Number(officeNumber)

    if (!trimmedName || !officeNumber.trim()) {
      alert('Please enter both office name and office number')
      return
    }

    if (!Number.isInteger(parsedOfficeNumber) || parsedOfficeNumber < 0) {
      alert('Office number must be a whole number')
      return
    }

    try {
      const response = await fetch(`${API_BASE}/api/office/save`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: trimmedName,
          officeNumber: parsedOfficeNumber
        })
      })

      if (response.ok) {
        alert('Office Saved Successfully')
        setOfficeName('')
        setOfficeNumber('')
        loadOffices()
      } else {
        const errorMsg = await parseErrorMessage(response, response.statusText)
        alert(`Saving Office failed: ${errorMsg}`)
      }
    } catch (error) {
      alert(`Unable to connect to the server. Please ensure the backend is running on port 8085. (${error.message})`)
    }
  }

  async function deleteByNumber(numberToDelete) {
    const parsed = Number(numberToDelete)
    if (!Number.isInteger(parsed) || parsed < 0) {
      alert('Office number must be a whole number')
      return
    }

    try {
      const officesResponse = await fetch(`${API_BASE}/api/offices`)
      if (!officesResponse.ok) {
        const errorMsg = await parseErrorMessage(officesResponse, officesResponse.statusText)
        alert(`Unable to load offices: ${errorMsg}`)
        return
      }

      const offices = await officesResponse.json()
      const office = offices.find(
        (item) => Number(item.officeNumber) === parsed
      )

      if (!office) {
        alert(`No office found with number ${parsed}`)
        return
      }

      const response = await fetch(
        `${API_BASE}/api/office/${encodeURIComponent(office.id)}`,
        { method: 'DELETE' }
      )

      if (response.ok || response.status === 204) {
        alert('Office Deleted Successfully')
        setOffNumber('')
        loadOffices()
      } else {
        const errorMsg = await parseErrorMessage(response, response.statusText)
        alert(`Deleting Office failed: ${errorMsg}`)
      }
    } catch (error) {
      alert(`Unable to connect to the server. Please ensure the backend is running on port 8085. (${error.message})`)
    }
  }

  async function deleteOffice() {
    if (!offNumber.trim()) {
      alert('Please enter an office number')
      return
    }
    await deleteByNumber(offNumber)
  }

  async function deleteOfficeById(officeId, officeIdentifier) {
    const confirmDelete = window.confirm(`Are you sure you want to delete office ${officeIdentifier}?`)
    if (!confirmDelete) return

    try {
      const response = await fetch(
        `${API_BASE}/api/office/${encodeURIComponent(officeId)}`,
        { method: 'DELETE' }
      )

      if (response.ok || response.status === 204) {
        alert('Office Deleted Successfully')
        loadOffices()
      } else {
        const errorMsg = await parseErrorMessage(response, response.statusText)
        alert(`Deleting Office failed: ${errorMsg}`)
      }
    } catch (error) {
      alert(`Unable to connect to the server: ${error.message}`)
    }
  }

  const filteredOffices = officesList.filter((item) => {
    const term = searchTerm.toLowerCase().trim()
    if (!term) return true
    const nameMatches = item.name && item.name.toLowerCase().includes(term)
    const numberMatches = item.officeNumber != null && String(item.officeNumber).toLowerCase().includes(term)
    return nameMatches || numberMatches
  })

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <LibraryHeader/>
      <h1>Save OFFICE</h1>
      <div style={{ marginBottom: '10px' }}>
        <label>Office Name: </label>
        <input
          type="text"
          value={officeName}
          placeholder="e.g. Room 201"
          onChange={(e) => setOfficeName(e.target.value)}
        />
      </div>
      <div style={{ marginBottom: '10px' }}>
        <label>Office Number: </label>
        <input
          type="number"
          value={officeNumber}
          placeholder="e.g. 201"
          onChange={(e) => setOfficeNumber(e.target.value)}
        />
      </div>
      <LibraryButton buttonLabel="SAVE" onClickButton={saveOffice} />

      <hr style={{ margin: '25px 0' }} />

      <h2>Delete Office</h2>
      <div style={{ marginBottom: '10px' }}>
        <label>Office Number: </label>
        <input
          type="number"
          value={offNumber}
          placeholder="Enter office number to delete"
          onChange={(e) => setOffNumber(e.target.value)}
        />
      </div>
      <LibraryButton buttonLabel="DELETE" onClickButton={deleteOffice} />

      <hr style={{ margin: '25px 0' }} />

      <h2>Existing Offices ({filteredOffices.length})</h2>

      <div style={{ marginBottom: '15px', display: 'flex', alignItems: 'center', gap: '8px' }}>
        <label>Search Office: </label>
        <input
          type="text"
          value={searchTerm}
          placeholder="Search by name or number..."
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ padding: '6px 10px', width: '250px' }}
        />
        {searchTerm && (
          <button
            type="button"
            onClick={() => setSearchTerm('')}
            style={{ padding: '5px 10px', cursor: 'pointer' }}
          >
            Clear
          </button>
        )}
      </div>

      {isLoading ? (
        <p>Loading offices...</p>
      ) : filteredOffices.length === 0 ? (
        <p>No offices found.</p>
      ) : (
        <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse', marginTop: '10px' }}>
          <thead>
            <tr style={{ background: '#f2f2f2' }}>
              <th>Office Number</th>
              <th>Office Name</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredOffices.map((item) => (
              <tr key={item.id}>
                <td style={{ textAlign: 'center', fontWeight: 'bold' }}>{item.officeNumber}</td>
                <td>{item.name || '—'}</td>
                <td style={{ textAlign: 'center' }}>
                  <button
                    type="button"
                    onClick={() => alert('Update failed: The operation conflicts with related records.')}
                    style={{ background: '#1890ff', color: '#fff', border: 'none', padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', marginRight: '6px' }}
                  >
                    Update
                  </button>
                  <button
                    type="button"
                    onClick={() => deleteOfficeById(item.id, item.name || item.officeNumber)}
                    style={{ background: '#ff4d4f', color: '#fff', border: 'none', padding: '4px 10px', borderRadius: '4px', cursor: 'pointer' }}
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

export default Office
