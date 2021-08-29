import styled from 'styled-components';

const Wrapper = styled.div`
  display: flex
  margin-left: 1rem;
  align-self: stretch;
  align-items: stretch;
  color: inherit;
`;

export const WindowButtonsMac = ({

}) => {
  return (<Wrapper>
    <button>close</button>
    <button>minimize</button>
    <button>maximize</button>
    <button>restore</button>
  </Wrapper>)
}