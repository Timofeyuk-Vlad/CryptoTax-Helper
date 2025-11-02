import React from 'react';
import { useAuth } from '../context/AuthContext';

const Profile = () => { const { currentUser } = useAuth();

    return (
        <div
        style={containerStyle}>
            <h1>Профиль пользователя</h1>
            <div style={profileCardStyle}>
                <p>
                    <strong>Email:</strong>
                    {currentUser?.email}
                </p>
                <p>
                    <strong>Статус:</strong>
                    Активный
                </p>
            </div>
        </div>
    );

};

const containerStyle = { maxWidth: '1200px', margin: '0 auto', padding: '2rem' };

const profileCardStyle = { backgroundColor: '#f8f9fa', padding: '2rem', borderRadius: '8px', marginTop: '2rem' };

export default Profile;