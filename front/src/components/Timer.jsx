import React, { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';

const TimerText = styled.span`
  font-size: 0.95rem;
  min-width: 55px;
  text-align: center;
  font-variant-numeric: tabular-nums;
  color: ${({ $danger }) => ($danger ? '#d32f2f' : '#1976d2')};
`;

const Timer = ({ seconds = 180, isActive, onTimeout, colorChangeSec = 30 }) => {
  const [time, setTime] = useState(seconds);
  const timerRef = useRef(null);

  useEffect(() => {
    if (isActive && time > 0) {
      timerRef.current = setTimeout(() => setTime(time - 1), 1000);
    } else if (isActive && time === 0) {
      if (onTimeout) onTimeout();
    }
    return () => clearTimeout(timerRef.current);
  }, [isActive, time, onTimeout]);

  useEffect(() => {
    if (!isActive) setTime(seconds);
  }, [isActive, seconds]);

  const formatTime = (sec) => {
    const m = String(Math.floor(sec / 60)).padStart(2, '0');
    const s = String(sec % 60).padStart(2, '0');
    return `${m}:${s}`;
  };

  return (
    <TimerText $danger={time <= colorChangeSec}>
      {formatTime(time)}
    </TimerText>
  );
};

export default Timer; 