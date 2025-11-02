import React from 'react';

const Subscriptions = () => {
    return (
        <div style={containerStyle}>
            <h1>Мои подписки</h1>
            <p>Здесь будут ваши подписки на рейсы...</p>
        </div>
    );
};

const containerStyle = {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '2rem'
};

export default Subscriptions;