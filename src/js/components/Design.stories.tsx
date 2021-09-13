import styled, { css } from 'styled-components';
import { readableColor } from 'polished';
import { permuteColorOpacities, themeLight, themeDark } from '../style/style'

export default {
  title: 'Design',
  argTypes: {},
  parameters: {
    layout: 'fullscreen'
  }
};

const Stack = styled.div`
  width: 40em;
  margin: 4em auto;

  h2 {
    margin: 0;
    text-align: center;
  }
`;

const Title = styled.div`
  font-weight: bold;
  font-size: var(--font-size--text-xl);
`;

const Description = styled.div``

const Wrapper = styled.div`
  padding: 2rem;
  display: flex;
  gap: 1rem;
  flex-direction: column;

  p {
    margin: 0;
  }
  
  code {
    color: var(--link-color);
    padding: 0.25rem 0.5rem;
    background: var(--background-minus-2);
    border-radius: 0.25rem;
    font-size: 0.75rem;
    font-family: var(--font-family-code);
    user-select: all;
  }
`;

const ColorInstance = styled.div`
  width: 3rem;
  height: 3rem;
  flex: 0 0 3rem;
  color: var(--background);
  position: relative;
  border-radius: 100em;
  transition: all 0.12s ease-in-out;

  &:after,
  &:before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: var(--background);
    opacity: var(--opacity);
    transition: all 0.12s ease-in-out;
  }

  &:before {
    opacity: 0;
    background: var(--contrast-background, var(--background-color));
    inset: 0.25rem;
    filter: blur(0.25rem);
    transition: all 0.12s ease-in-out;
  }

  &:hover {
    transform: scale(1.1);
    z-index: 10;
    box-shadow: var(--depth-shadow-8);

    &:before {
      opacity: 0.7;
    }
  }
`;

const ColorStack = styled.div`
  display: flex;

  ${props => props.hasContrast && css`
    padding: 0.75rem 1.5rem 0.75rem 1rem;
    border-radius: 100em;
    background: var(--body-text-color);
    --contrast-background: var(--body-text-color);
    width: max-content;
  `}

  > * {
    margin-inline-end: -0.5rem;
  }
`;

const DepthStack = styled.div`
  display: flex;
  gap: 1rem;
`;

const ColorDemo = ({ name, color, description, hasContrast = false }) => <Wrapper>
  <header>
    <Title>{name}</Title>
    <Description>{description}</Description>
    <code>{color}</code>
  </header>
  <ColorStack hasContrast={hasContrast}>
    <ColorInstance style={{ "--background": color, "--opacity": 1 }} ></ColorInstance>
    <ColorInstance style={{ "--background": color, "--opacity": 0.85 }} ></ColorInstance>
    <ColorInstance style={{ "--background": color, "--opacity": 0.75 }} ></ColorInstance>
    <ColorInstance style={{ "--background": color, "--opacity": 0.5 }} ></ColorInstance>
    <ColorInstance style={{ "--background": color, "--opacity": 0.25 }} ></ColorInstance>
    <ColorInstance style={{ "--background": color, "--opacity": 0.1 }} ></ColorInstance>
  </ColorStack>
</Wrapper>


export const Design = () => <>
  <Stack>
    <h2>Intent colors</h2>
    <ColorDemo name="Link" description="Use for links and interactive elements that need additional color." color="var(--link-color)" />
    <ColorDemo name="Highlight" description="Use to call out an element." color="var(--highlight-color)" />
    <ColorDemo name="Warning" description="Use when a destructive action or message needs additional color." color="var(--warning-color)" />
    <ColorDemo name="Confirmation" description="Use when a confirmation action or message needs additional color. " color="var(--confirmation-color)" />
    <h2>Interface colors</h2>
    <ColorDemo name="Heading text" description="Use for heading text color." color="var(--header-text-color)" />
    <ColorDemo name="Body text" description="Use for body text color. Some interactive elements may also use this color when they don't require additional color to stand out." color="var(--body-text-color)" />
    <ColorDemo name="Border" description="Use for borders separating elements." color="var(--border-color)" />
    <ColorDemo name="Shadow" description="Use for shadows throughout the app." color="var(--shadow-color)" />
    <ColorDemo hasContrast name="Background plus 2" description="test" color="var(--background-plus-2)" />
    <ColorDemo hasContrast name="Background plus 1" description="test" color="var(--background-plus-1)" />
    <ColorDemo hasContrast name="Background color" description="test" color="var(--background-color)" />
    <ColorDemo hasContrast name="Background minus 1" description="test" color="var(--background-minus-1)" />
    <ColorDemo hasContrast name="Background minus 2" description="test" color="var(--background-minus-2)" />
    <h2>Depth</h2>
    <Wrapper style={{ gap: "3rem" }}>
      <header>
        <h3>Depth Shadows</h3>
        <Description>Use shadows sparingly. Shadows may also be paired with a 1px shadow on the same element to better cut it out of its context.</Description>
      </header>
      <DepthStack>
        <div style={{ boxShadow: "var(--depth-shadow-4)", width: "4em", height: "4em" }} />
        <div style={{ boxShadow: "var(--depth-shadow-8)", width: "4em", height: "4em" }} />
        <div style={{ boxShadow: "var(--depth-shadow-16)", width: "4em", height: "4em" }} />
        <div style={{ boxShadow: "var(--depth-shadow-64)", width: "4em", height: "4em" }} />
      </DepthStack>
    </Wrapper>
    <hr />
  </Stack>
</>