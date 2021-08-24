import styled from 'styled-components';

export const Body = styled.main`
  display: grid;
  grid-template-areas: 'above above above above'
                       'toggle bullet content refs'
                       'below below below below';
  grid-template-columns: "1em 1em 1fr auto";
  grid-template-rows: 0 1fr 0;
  border-radius: 0.5rem;
  position: relative;
`;
