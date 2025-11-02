import React from 'react';
import FlightList from '../components/flights/FlightList';

const Flights = () => {
    return (
        <div style={containerStyle}>
            <h1>Список рейсов</h1>
            <FlightList />
        </div>
    );
};

const containerStyle = {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '2rem'
};

export default Flights;