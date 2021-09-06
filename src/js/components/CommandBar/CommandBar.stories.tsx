import { CommandBar } from './CommandBar';
import { BADGE } from '../../storybook';

export default {
  title: 'Components/CommandBar',
  component: CommandBar,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
};

const Template = (args) => <CommandBar {...args} isModal={false} />;

export const Empty = Template.bind({});
Empty.args = {
  isOpen: true,
};

export const FilledWithNoResults = Template.bind({});
FilledWithNoResults.args = {
  defaultQuery: 'animal',
};

export const ManyResults = Template.bind({});
ManyResults.args = {
  defaultQuery: 'animal',
  results: [{
    title: "Chital",
    isSelected: true,
    preview: "Pellentesque eget nunc. Donec quis orci eget orci vehicula condimentum. Curabitur in libero ut massa volutpat convallis. Morbi odio odio, elementum eu, interdum eu, tincidunt in, leo. Maecenas pulvinar lobortis est. Phasellus sit amet erat. Nulla tempus."
  }, {
    title: "Fox, north american red",
    preview: "Suspendisse potenti. Nullam porttitor lacus at turpis. Donec posuere metus vitae ipsum. Aliquam non mauris. Morbi non lectus. Aliquam sit amet diam in magna bibendum imperdiet. Nullam orci pede, venenatis non, sodales sed, tincidunt eu, felis. Fusce posuere felis sed lacus. Morbi sem mauris, laoreet ut, rhoncus aliquet, pulvinar sed, nisl."
  }, {
    title: "Hawk, red-tailed",
    preview: "Phasellus in felis. Donec semper sapien a libero. Nam dui."
  }, {
    title: "Long-tailed skua",
    preview: "Cras pellentesque volutpat dui."
  }, {
    title: "Wolf spider",
    preview: "Vivamus in felis eu sapien cursus vestibulum. Proin eu mi. Nulla ac enim."
  }, {
    title: "Amazon parrot (unidentified)",
    preview: "Vivamus tortor. Duis mattis egestas metus. Aenean fermentum. Donec ut mauris eget massa tempor convallis. Nulla neque libero, convallis eget, eleifend luctus, ultricies eu, nibh. Quisque id justo sit amet sapien dignissim vestibulum."
  }]
};
