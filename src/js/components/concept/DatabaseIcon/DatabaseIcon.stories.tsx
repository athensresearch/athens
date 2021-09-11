import styled from 'styled-components';
import { BADGE, Storybook } from '../../../storybook';

import { DatabaseIcon } from './DatabaseIcon';

export default {
    title: 'Components/DatabaseIcon',
    component: DatabaseIcon,
    argTypes: {
        color: { control: { type: 'color' } },
    },
    parameters: {
        badges: [BADGE.DEV, BADGE.IN_USE]
    },
    decorators: [(Story) => <Storybook.Wrapper style={{ fontSize: "2em" }}><Story /></Storybook.Wrapper>]
};

const Template = (args) => <DatabaseIcon
    {...args} />;

export const Default = Template.bind({});
Default.args = {
    name: 'Untitled Database'
};

const Grid = styled.div`
    display: grid;
    margin: auto;
    gap: 1em;
    grid-template-columns: repeat(6, 1fr);
`;

export const Sizes = () => <>
    <DatabaseIcon size="0.5em" name="R" />
    <DatabaseIcon size="1em" name="L" />
    <DatabaseIcon size="1.5em" name="S" />
    <DatabaseIcon size="2em" name="T" />
    <DatabaseIcon size="2.5em" name="N" />
    <DatabaseIcon size="3em" name="E" />
    <DatabaseIcon size="3.5em" name="R" />
    <DatabaseIcon size="4em" name="L" />
    <DatabaseIcon size="4.5em" name="S" />
    <DatabaseIcon size="5em" name="T" />
    <DatabaseIcon size="5.5em" name="N" />
    <DatabaseIcon size="6em" name="E" />
</>
Sizes.decorators = [(Story) => <Grid><Story /></Grid>];

export const Styles = () => <>
    <DatabaseIcon size="2em" name="R" color="#0075E1" />
    <DatabaseIcon size="2em" name="L" color="#000" />
    <DatabaseIcon size="2em" name="S" color="#FE4A49" />
    <DatabaseIcon size="2em" name="T" color="#FED766" />
    <DatabaseIcon size="2em" name="N" color="#2B2D42" />
    <DatabaseIcon size="2em" name="E" color="#B8F2E6" />
    <DatabaseIcon size="2em" name="R" icon='🔥' color="#0075E1" />
    <DatabaseIcon size="2em" name="L" icon="🎉" color="#000" />
    <DatabaseIcon size="2em" name="S" icon="💲" color="#FE4A49" />
    <DatabaseIcon size="2em" name="T" icon="🏠" color="#FED766" />
    <DatabaseIcon size="2em" name="N" icon="👻" color="#2B2D42" />
    <DatabaseIcon size="2em" name="E" icon="🤖" color="#B8F2E6" />
</>;
Styles.decorators = [(Story) => <Grid><Story /></Grid>];
