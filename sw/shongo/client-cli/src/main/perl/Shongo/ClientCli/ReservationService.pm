#
# Management of reservations.
#
# @author Martin Srom <martin.srom@cesnet.cz>
#
package Shongo::ClientCli::ReservationService;

use strict;
use warnings;
use Text::Table;

use Shongo::Common;
use Shongo::Console;
use Shongo::Shell;
use Shongo::ClientCli::API::ReservationRequestAbstract;
use Shongo::ClientCli::API::Reservation;
use Shongo::ClientCli::API::Alias;

#
# Populate shell by options for management of reservations.
#
# @param shell
#
sub populate()
{
    my ($self, $shell) = @_;
    $shell->add_commands({
        'create-reservation-request' => {
            desc => 'Create a new reservation request',
            args => '[<json_attributes>]',
            method => sub {
                my ($shell, $params, @args) = @_;
                my $attributes = Shongo::Shell::parse_attributes($params);
                if ( defined($attributes) ) {
                    create_reservation_request($attributes, $params->{'options'});
                }
            }
        },
        'modify-reservation-request' => {
            desc => 'Modify an existing reservation request',
            args => '[id] [<json_attributes>]',
            method => sub {
                my ($shell, $params, @args) = @_;
                my $attributes = Shongo::Shell::parse_attributes($params);
                if ( defined($attributes) ) {
                    modify_reservation_request($args[0], $attributes, $params->{'options'});
                }
            }
        },
        'delete-reservation-request' => {
            desc => 'Delete an existing reservation request',
            args => '[id]',
            method => sub {
                my ($shell, $params, @args) = @_;
                if (defined($args[0])) {
                    foreach my $id (split(/,/, $args[0])) {
                        delete_reservation_request($id);
                    }
                } else {
                    delete_reservation_request();
                }
            }
        },
        'list-reservation-requests' => {
            desc => 'List summary of all existing reservation requests',
            options => 'owner=s technology=s',
            args => '[-owner=*|<user-id>] [-technology]',
            method => sub {
                my ($shell, $params, @args) = @_;
                list_reservation_requests($params->{'options'});
            }
        },
        'get-reservation-request' => {
            desc => 'Get existing reservation request',
            args => '[id]',
            method => sub {
                my ($shell, $params, @args) = @_;
                if (defined($args[0])) {
                    foreach my $id (split(/,/, $args[0])) {
                        get_reservation_request($id);
                    }
                } else {
                    get_reservation_request();
                }
            }
        },
        'get-reservation-for-request' => {
            desc => 'Get allocated reservations for existing reservation request',
            args => '[id]',
            method => sub {
                my ($shell, $params, @args) = @_;
                if (defined($args[0])) {
                    foreach my $id (split(/,/, $args[0])) {
                        get_reservation_for_request($id);
                    }
                } else {
                    get_reservation_for_request();
                }
            }
        },
        'get-reservation' => {
            desc => 'Get existing reservation',
            args => '[id]',
            method => sub {
                my ($shell, $params, @args) = @_;
                if (defined($args[0])) {
                    foreach my $id (split(/,/, $args[0])) {
                        get_reservation($id);
                    }
                } else {
                    get_reservation();
                }
            }
        },
    });
}

sub select_reservation_request
{
    my ($id, $attributes) = @_;
    if ( defined($attributes) && defined($attributes->{'id'}) ) {
        $id = $attributes->{'id'};
    }
    $id = console_read_value('Identifier of the reservation request', 0, $Shongo::Common::IdPattern, $id);
    return $id;
}

sub create_reservation_request()
{
    my ($attributes, $options) = @_;

    $options->{'on_confirm'} = sub {
        my ($reservation_request) = @_;
        console_print_info("Creating reservation request...");
        my $response = Shongo::ClientCli->instance()->secure_request(
            'Reservation.createReservationRequest',
            $reservation_request->to_xml()
        );
        if ( !$response->is_fault() ) {
            return $response->value();
        }
        return undef;
    };

    my $id = Shongo::ClientCli::API::ReservationRequestAbstract->create($attributes, $options);
    if ( defined($id) ) {
        console_print_info("Reservation request '%s' successfully created.", $id);
    }
}

sub modify_reservation_request()
{
    my ($id, $attributes, $options) = @_;
    $id = select_reservation_request($id, $attributes);
    if ( !defined($id) ) {
        return;
    }
    my $result = Shongo::ClientCli->instance()->secure_request(
        'Reservation.getReservationRequest',
        RPC::XML::string->new($id)
    );

    $options->{'on_confirm'} = sub {
        my ($reservation_request) = @_;
        console_print_info("Modifying reservation request...");
        my $response = Shongo::ClientCli->instance()->secure_request(
            'Reservation.modifyReservationRequest',
            $reservation_request->to_xml()
        );
        if ( !$response->is_fault() ) {
            return $reservation_request->{'id'};
        }
        return undef;
    };

    if ( !$result->is_fault ) {
        my $reservation_request = Shongo::ClientCli::API::ReservationRequestAbstract->from_hash($result);
        if ( defined($reservation_request) ) {
            $reservation_request->modify($attributes, $options);
        }
    }
}

sub delete_reservation_request()
{
    my ($id) = @_;
    $id = select_reservation_request($id);
    if ( !defined($id) ) {
        return;
    }
    Shongo::ClientCli->instance()->secure_request(
        'Reservation.deleteReservationRequest',
        RPC::XML::string->new($id)
    );
}

sub list_reservation_requests()
{
    my ($options) = @_;
    my $filter = {};
    if ( defined($options->{'technology'}) ) {
        $filter->{'technology'} = [];
        foreach my $technology (split(/,/, $options->{'technology'})) {
            $technology =~ s/(^ +)|( +$)//g;
            push(@{$filter->{'technology'}}, $technology);
        }
    }
    if ( defined($options->{'owner'}) ) {
        $filter->{'userId'} = $options->{'owner'};
    }
    my $application = Shongo::ClientCli->instance();
    my $response = $application->secure_request('Reservation.listReservationRequests', $filter);
    if ( $response->is_fault() ) {
        return
    }
    my $table = Text::Table->new(
        \'| ', 'Identifier',
        \' | ', 'Owner',
        \' | ', 'Created',
        \' | ', 'Type',
        \' | ', 'Name',
        #\' | ', 'Purpose',
        \' | ', 'Earliest Slot', \' |'
    );
    my $Type = {
        'NORMAL' => 'Normal',
        'PERMANENT' => 'Permanent'
    };
    foreach my $reservation_request (@{$response->value()}) {
        $table->add(
            $reservation_request->{'id'},
            $application->format_user($reservation_request->{'userId'}),
            format_datetime($reservation_request->{'created'}),
            $Type->{$reservation_request->{'type'}},
            $reservation_request->{'name'},
            #$Shongo::ClientCli::API::ReservationRequest::Purpose->{$reservation_request->{'purpose'}},
            format_interval($reservation_request->{'earliestSlot'})
        );
    }
    console_print_table($table);
}

sub get_reservation_request()
{
    my ($id) = @_;
    $id = select_reservation_request($id);
    if ( !defined($id) ) {
        return;
    }
    my $result = Shongo::ClientCli->instance()->secure_request(
        'Reservation.getReservationRequest',
        RPC::XML::string->new($id)
    );
    if ( !$result->is_fault ) {
        my $reservation_request = Shongo::ClientCli::API::ReservationRequestAbstract->from_hash($result);
        if ( defined($reservation_request) ) {
            console_print_text($reservation_request->to_string());
        }
    }
}

sub get_reservation_for_request()
{
    my ($id) = @_;
    $id = select_reservation_request($id);
    if ( !defined($id) ) {
        return;
    }
    my $result = Shongo::ClientCli->instance()->secure_request(
        'Reservation.listReservations',
        RPC::XML::string->new($id)
    );
    if ( $result->is_fault ) {
        return;
    }
    my $reservations = $result->value();
    if (get_collection_size($reservations) == 0) {
        return;
    }
    print("\n");
    my $index = 0;
    foreach my $reservationXml (@{$reservations}) {
        my $reservation = Shongo::ClientCli::API::Reservation->from_hash($reservationXml);
        $reservation->fetch_child_reservations(1);
        $index++;
        printf(" %d) %s\n", $index, text_indent_lines($reservation->to_string(), 4, 0));
    }
}

sub select_reservation($)
{
    my ($id) = @_;
    $id = console_read_value('Identifier of the reservation', 0, $Shongo::Common::IdPattern, $id);
    return $id;
}


sub get_reservation()
{
    my ($id) = @_;
    $id = select_reservation($id);
    if ( !defined($id) ) {
        return;
    }
    my $result = Shongo::ClientCli->instance()->secure_request(
        'Reservation.getReservation',
        RPC::XML::string->new($id)
    );
    if ( !$result->is_fault ) {
        my $reservation = Shongo::ClientCli::API::Reservation->from_hash($result);
        $reservation->fetch_child_reservations(1);
        if ( defined($reservation) ) {
            console_print_text($reservation->to_string());
        }
    }
}

1;