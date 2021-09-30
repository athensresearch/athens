import styled from 'styled-components';

export const Backdrop = styled.div`
  position: fixed;
  z-index: var(--zindex-modal);
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background-color: ${props => props.hidden ? 'none' : 'rgba(0, 0, 0, 0.5)'};
  display: flex;
  align-items: center;
  justify-content: center;
`;